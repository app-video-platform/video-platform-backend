package com.myproject.video.video_platform.service.product.membership;

import com.fasterxml.jackson.databind.JsonNode;
import com.myproject.video.video_platform.common.enums.membership.MembershipContentStatus;
import com.myproject.video.video_platform.common.enums.membership.MembershipContentType;
import com.myproject.video.video_platform.common.enums.membership.MembershipFeedEntryKind;
import com.myproject.video.video_platform.common.enums.membership.MembershipOrderingMode;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.membership.MembershipDtos;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.products.membership.MembershipContent;
import com.myproject.video.video_platform.entity.products.membership.MembershipFeedEntry;
import com.myproject.video.video_platform.entity.products.membership.MembershipProduct;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.product.UnsupportedProductOperationException;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.repository.products.membership.MembershipContentRepository;
import com.myproject.video.video_platform.repository.products.membership.MembershipFeedEntryRepository;
import com.myproject.video.video_platform.repository.products.membership.MembershipProductRepository;
import com.myproject.video.video_platform.service.product.ProductAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MembershipService {
    private static final long MAX_ASSET_SIZE = 500L * 1024L * 1024L;

    private final ProductRepository productRepository;
    private final MembershipProductRepository membershipRepository;
    private final MembershipContentRepository contentRepository;
    private final MembershipFeedEntryRepository feedRepository;
    private final ProductAuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public MembershipDtos.Aggregate getAggregate(UUID productId) {
        MembershipProduct membership = requireMembership(productId);
        authorizationService.requireOwnerOrAdmin(membership);
        return aggregate(membership);
    }

    @Transactional
    public MembershipDtos.Aggregate updateConfig(UUID productId, JsonNode request) {
        MembershipProduct membership = requireOwnedMembership(productId);
        if (request != null && request.hasNonNull("orderingMode")) {
            MembershipOrderingMode next = enumValue(
                    MembershipOrderingMode.class,
                    request.get("orderingMode").asText(),
                    "orderingMode"
            );
            if (next != membership.getOrderingMode()) {
                membership.setOrderingMode(next);
                normalizeStoredOrdering(membership, feedRepository.findAllByMembershipId(productId));
            }
        }
        touch(membership);
        return aggregate(membership);
    }

    @Transactional
    public MembershipDtos.Content createContent(UUID productId, JsonNode request) {
        MembershipProduct membership = requireOwnedMembership(productId);
        if (request == null || !request.isObject()) {
            throw new IllegalArgumentException("Membership content payload is required");
        }

        MembershipContent content = new MembershipContent();
        content.setMembership(membership);
        content.setType(enumValue(MembershipContentType.class, requiredText(request, "type"), "type"));
        content.setTitle(requiredText(request, "title"));
        content.setDescription(optionalText(request, "description"));
        content.setStatus(enumValue(MembershipContentStatus.class, requiredText(request, "status"), "status"));
        applyTypePayload(content, request, true);
        validateContent(content);
        content = contentRepository.saveAndFlush(content);

        List<MembershipFeedEntry> entries = feedRepository.findAllByMembershipId(productId);
        if (membership.getOrderingMode() == MembershipOrderingMode.MANUAL) {
            entries.forEach(entry -> entry.setPosition(entry.getPosition() == null ? 2 : entry.getPosition() + 1));
            feedRepository.saveAll(entries);
        }
        MembershipFeedEntry entry = new MembershipFeedEntry();
        entry.setMembership(membership);
        entry.setKind(MembershipFeedEntryKind.CONTENT);
        entry.setContent(content);
        entry.setAddedAt(Instant.now());
        entry.setPosition(membership.getOrderingMode() == MembershipOrderingMode.MANUAL ? 1 : null);
        feedRepository.save(entry);
        touch(membership);
        return mapContent(content, entry.getPosition());
    }

    @Transactional
    public MembershipDtos.Content updateContent(UUID productId, UUID contentId, JsonNode request) {
        MembershipProduct membership = requireOwnedMembership(productId);
        MembershipContent content = contentRepository.findByIdAndMembershipId(contentId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership content not found: " + contentId));
        if (request == null || !request.isObject()) {
            throw new IllegalArgumentException("Membership content payload is required");
        }
        if (request.hasNonNull("type")) {
            MembershipContentType requestedType = enumValue(
                    MembershipContentType.class, request.get("type").asText(), "type");
            if (requestedType != content.getType()) {
                throw conflict("Membership content type cannot be changed");
            }
        }
        if (request.has("title")) {
            content.setTitle(requiredText(request, "title"));
        }
        if (request.has("description")) {
            content.setDescription(optionalText(request, "description"));
        }
        if (request.hasNonNull("status")) {
            content.setStatus(enumValue(
                    MembershipContentStatus.class, request.get("status").asText(), "status"));
        }
        applyTypePayload(content, request, false);
        validateContent(content);
        content = contentRepository.saveAndFlush(content);
        touch(membership);
        MembershipFeedEntry contentEntry = feedRepository.findAllByMembershipId(productId).stream()
                .filter(entry -> entry.getContent() != null && entry.getContent().getId().equals(contentId))
                .findFirst()
                .orElse(null);
        Integer position = contentEntry == null ? null : contentEntry.getPosition();
        return mapContent(content, position);
    }

    @Transactional
    public void deleteContent(UUID productId, UUID contentId) {
        MembershipProduct membership = requireOwnedMembership(productId);
        MembershipContent content = contentRepository.findByIdAndMembershipId(contentId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership content not found: " + contentId));
        feedRepository.deleteByMembershipIdAndContentId(productId, contentId);
        contentRepository.delete(content);
        contentRepository.flush();
        normalizeStoredOrdering(membership, feedRepository.findAllByMembershipId(productId));
        touch(membership);
    }

    @Transactional
    public MembershipDtos.Aggregate replaceFeed(UUID productId, JsonNode request) {
        MembershipProduct membership = requireOwnedMembership(productId);
        if (request == null || !request.isObject() || !request.has("feed") || !request.get("feed").isArray()) {
            throw new IllegalArgumentException("Membership feed array is required");
        }
        MembershipOrderingMode orderingMode = enumValue(
                MembershipOrderingMode.class, requiredText(request, "orderingMode"), "orderingMode");
        List<MembershipContent> content = contentRepository.findAllByMembershipId(productId);
        Map<UUID, MembershipContent> contentById = new HashMap<>();
        content.forEach(item -> contentById.put(item.getId(), item));

        Map<String, MembershipFeedEntry> existing = new HashMap<>();
        for (MembershipFeedEntry entry : feedRepository.findAllByMembershipId(productId)) {
            existing.put(identity(entry), entry);
        }

        List<MembershipFeedEntry> replacement = new ArrayList<>();
        Set<String> identities = new HashSet<>();
        Set<UUID> referencedContentIds = new HashSet<>();
        int requestPosition = 1;
        for (JsonNode node : request.get("feed")) {
            MembershipFeedEntryKind kind = enumValue(
                    MembershipFeedEntryKind.class, requiredText(node, "kind"), "feed.kind");
            MembershipFeedEntry entry;
            String identity;
            if (kind == MembershipFeedEntryKind.CONTENT) {
                UUID contentId = requiredUuid(node, "contentId");
                MembershipContent target = contentById.get(contentId);
                if (target == null) {
                    throw conflict("Feed content must belong to this Membership: " + contentId);
                }
                identity = "content:" + contentId;
                entry = existing.getOrDefault(identity, new MembershipFeedEntry());
                entry.setContent(target);
                entry.setIncludedProductId(null);
                referencedContentIds.add(contentId);
            } else {
                UUID includedProductId = requiredUuid(node, "productId");
                validateIncludedProduct(membership, includedProductId);
                identity = "product:" + includedProductId;
                entry = existing.getOrDefault(identity, new MembershipFeedEntry());
                entry.setContent(null);
                entry.setIncludedProductId(includedProductId);
            }
            if (!identities.add(identity)) {
                throw conflict("Duplicate Membership feed entry: " + identity);
            }
            entry.setMembership(membership);
            entry.setKind(kind);
            if (entry.getAddedAt() == null) {
                entry.setAddedAt(Instant.now());
            }
            entry.setPosition(orderingMode == MembershipOrderingMode.MANUAL ? requestPosition : null);
            replacement.add(entry);
            requestPosition++;
        }
        if (!referencedContentIds.equals(contentById.keySet())) {
            throw conflict("The feed must contain every native Membership content item exactly once");
        }

        List<MembershipFeedEntry> removed = existing.entrySet().stream()
                .filter(entry -> !identities.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        feedRepository.deleteAll(removed);
        feedRepository.saveAll(replacement);
        membership.setOrderingMode(orderingMode);
        touch(membership);
        return aggregate(membership);
    }

    private MembershipProduct requireOwnedMembership(UUID productId) {
        MembershipProduct membership = requireMembership(productId);
        authorizationService.requireOwnerOrAdmin(membership);
        return membership;
    }

    private MembershipProduct requireMembership(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (!(product instanceof MembershipProduct membership)) {
            throw conflict("Product is not a Membership: " + productId);
        }
        return membership;
    }

    private void validateIncludedProduct(MembershipProduct membership, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> conflict("Included Product does not exist: " + productId));
        if (!product.getUser().getUserId().equals(membership.getUser().getUserId())) {
            throw conflict("Included Products must belong to the Membership owner");
        }
        if (product.getType() != ProductType.COURSE && product.getType() != ProductType.DOWNLOAD) {
            throw conflict("Only Course and Download Products can be included in a Membership");
        }
    }

    private MembershipDtos.Aggregate aggregate(MembershipProduct membership) {
        List<MembershipFeedEntry> entries = feedRepository.findAllByMembershipId(membership.getId());
        sort(entries, membership.getOrderingMode());
        Map<UUID, Integer> positions = new LinkedHashMap<>();
        entries.stream()
                .filter(entry -> entry.getContent() != null)
                .forEach(entry -> positions.put(entry.getContent().getId(), entry.getPosition()));
        Map<UUID, MembershipContent> contentById = new HashMap<>();
        contentRepository.findAllByMembershipId(membership.getId())
                .forEach(item -> contentById.put(item.getId(), item));
        List<MembershipDtos.Content> content = entries.stream()
                .filter(entry -> entry.getContent() != null)
                .map(entry -> contentById.get(entry.getContent().getId()))
                .filter(java.util.Objects::nonNull)
                .map(item -> mapContent(item, positions.get(item.getId())))
                .toList();
        List<MembershipDtos.FeedEntry> feed = entries.stream().map(this::mapFeedEntry).toList();
        LocalDateTime updatedAt = membership.getUpdatedAt() == null
                ? membership.getCreatedAt() : membership.getUpdatedAt();
        return new MembershipDtos.Aggregate(
                membership.getId(),
                new MembershipDtos.Config(membership.getId(), membership.getOrderingMode().name()),
                content,
                feed,
                updatedAt
        );
    }

    private void normalizeStoredOrdering(MembershipProduct membership, List<MembershipFeedEntry> entries) {
        sort(entries, membership.getOrderingMode());
        for (int index = 0; index < entries.size(); index++) {
            entries.get(index).setPosition(
                    membership.getOrderingMode() == MembershipOrderingMode.MANUAL ? index + 1 : null);
        }
        feedRepository.saveAll(entries);
    }

    private void sort(List<MembershipFeedEntry> entries, MembershipOrderingMode mode) {
        if (mode == MembershipOrderingMode.MANUAL) {
            entries.sort(Comparator.comparing(
                    MembershipFeedEntry::getPosition,
                    Comparator.nullsLast(Integer::compareTo)
            ));
        } else {
            entries.sort(Comparator.comparing(
                    MembershipFeedEntry::getAddedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())
            ));
        }
    }

    private void applyTypePayload(MembershipContent content, JsonNode request, boolean creating) {
        if (content.getType() == MembershipContentType.POST) {
            if (creating || request.has("body")) {
                content.setBody(requiredText(request, "body"));
            }
            content.setAssetFileId(null);
            content.setAssetFileName(null);
            content.setAssetFileType(null);
            content.setAssetSize(null);
            return;
        }
        String field = content.getType() == MembershipContentType.VIDEO ? "video" : "file";
        if (creating || request.has(field)) {
            JsonNode asset = request.get(field);
            if (asset == null || !asset.isObject()) {
                throw new IllegalArgumentException(field + " metadata is required");
            }
            String fileName = requiredText(asset, "fileName");
            String fileType = requiredText(asset, "fileType");
            long size = requiredLong(asset, "size");
            boolean unchanged = content.getAssetFileId() != null
                    && content.getAssetFileName() != null
                    && content.getAssetFileName().equals(fileName)
                    && content.getAssetFileType().equals(fileType)
                    && content.getAssetSize() != null
                    && content.getAssetSize() == size
                    && asset.hasNonNull("fileId")
                    && content.getAssetFileId().toString().equals(asset.get("fileId").asText());
            content.setAssetFileId(unchanged ? content.getAssetFileId() : UUID.randomUUID());
            content.setAssetFileName(fileName);
            content.setAssetFileType(fileType);
            content.setAssetSize(size);
        }
        content.setBody(null);
    }

    private void validateContent(MembershipContent content) {
        if (content.getTitle() == null || content.getTitle().isBlank() || content.getTitle().trim().length() > 255) {
            throw new IllegalArgumentException("Content title is required and must not exceed 255 characters");
        }
        content.setTitle(content.getTitle().trim());
        if (content.getType() == MembershipContentType.POST) {
            if (content.getBody() == null || content.getBody().isBlank()) {
                throw new IllegalArgumentException("Post body is required");
            }
        } else if (content.getAssetFileId() == null
                || content.getAssetFileName() == null || content.getAssetFileName().isBlank()
                || content.getAssetFileName().length() > 255
                || content.getAssetFileType() == null || content.getAssetFileType().isBlank()
                || content.getAssetFileType().length() > 150
                || content.getAssetSize() == null || content.getAssetSize() <= 0
                || content.getAssetSize() > MAX_ASSET_SIZE) {
            throw new IllegalArgumentException("Asset metadata is invalid or exceeds the 500 MiB limit");
        }
    }

    private MembershipDtos.Content mapContent(MembershipContent content, Integer position) {
        MembershipDtos.AssetRef asset = content.getType() == MembershipContentType.POST ? null
                : new MembershipDtos.AssetRef(
                        content.getAssetFileId(), content.getAssetFileName(),
                        content.getAssetFileType(), content.getAssetSize());
        return new MembershipDtos.Content(
                content.getId(), content.getType().name(), content.getTitle(), content.getDescription(),
                content.getStatus().name(), content.getBody(),
                content.getType() == MembershipContentType.VIDEO ? asset : null,
                content.getType() == MembershipContentType.RESOURCE ? asset : null,
                content.getCreatedAt(), content.getUpdatedAt(), position
        );
    }

    private MembershipDtos.FeedEntry mapFeedEntry(MembershipFeedEntry entry) {
        UUID contentId = entry.getContent() == null ? null : entry.getContent().getId();
        UUID productId = entry.getIncludedProductId();
        return new MembershipDtos.FeedEntry(
                entry.getKind() == MembershipFeedEntryKind.CONTENT
                        ? "content:" + contentId : "product:" + productId,
                entry.getKind().name(), contentId, productId, entry.getAddedAt(), entry.getPosition()
        );
    }

    private String identity(MembershipFeedEntry entry) {
        return entry.getKind() == MembershipFeedEntryKind.CONTENT
                ? "content:" + entry.getContent().getId()
                : "product:" + entry.getIncludedProductId();
    }

    private void touch(MembershipProduct membership) {
        membership.setUpdatedAt(LocalDateTime.now());
        membershipRepository.save(membership);
    }

    private static String requiredText(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field) || !node.get(field).isTextual()
                || node.get(field).asText().isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return node.get(field).asText().trim();
    }

    private static String optionalText(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            return null;
        }
        if (!node.get(field).isTextual()) {
            throw new IllegalArgumentException(field + " must be text");
        }
        return node.get(field).asText();
    }

    private static long requiredLong(JsonNode node, String field) {
        if (!node.hasNonNull(field) || !node.get(field).isIntegralNumber()
                || !node.get(field).canConvertToLong()) {
            throw new IllegalArgumentException(field + " is required and must be an integer");
        }
        return node.get(field).asLong();
    }

    private static UUID requiredUuid(JsonNode node, String field) {
        try {
            return UUID.fromString(requiredText(node, field));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(field + " must be a UUID");
        }
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value, String field) {
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + field + ": " + value);
        }
    }

    private static UnsupportedProductOperationException conflict(String message) {
        return new UnsupportedProductOperationException(message);
    }
}

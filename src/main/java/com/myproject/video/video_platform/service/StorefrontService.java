package com.myproject.video.video_platform.service;

import com.myproject.video.video_platform.common.enums.StorefrontAppearance;
import com.myproject.video.video_platform.common.enums.StorefrontTypography;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.dto.storefront.StorefrontDtos;
import com.myproject.video.video_platform.dto.user.SocialMediaLinkResponse;
import com.myproject.video.video_platform.entity.StorefrontConfig;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.StorefrontConfigRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorefrontService {
    private static final StorefrontDtos.Theme DEFAULT_THEME = new StorefrontDtos.Theme(
            StorefrontAppearance.DARK, "#ffbd41", StorefrontTypography.MODERN);

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StorefrontConfigRepository configRepository;

    @Transactional(readOnly = true)
    public StorefrontDtos.Config getCreatorConfig() {
        UUID creatorId = currentUserService.getCurrentUserId();
        requireCreator(creatorId);
        return configRepository.findByCreatorUserId(creatorId).map(StorefrontService::configDto)
                .orElse(new StorefrontDtos.Config(null, null, List.of(), DEFAULT_THEME, null));
    }

    @Transactional
    public StorefrontDtos.Config updateCreatorConfig(StorefrontDtos.UpdateRequest request) {
        UUID creatorId = currentUserService.getCurrentUserId();
        User creator = requireCreator(creatorId);
        List<UUID> orderIds = parseUniqueIds(request.productOrderIds(), "productOrderIds");
        Map<UUID, Product> owned = ownedProducts(creator);
        orderIds.forEach(id -> requireOwned(id, owned));

        UUID featuredId = optionalId(request.featuredProductId(), "featuredProductId");
        if (featuredId != null) {
            Product featured = requireOwned(featuredId, owned);
            if (featured.getStatus() != ProductStatus.PUBLISHED) {
                throw new IllegalArgumentException("Featured Product must be published");
            }
        }

        StorefrontConfig config = configRepository.findByCreatorUserId(creatorId).orElseGet(() -> {
            StorefrontConfig created = new StorefrontConfig();
            created.setCreator(creator);
            return created;
        });
        config.setAppearance(request.theme().appearance());
        config.setAccentColor(request.theme().accentColor().toLowerCase());
        config.setTypography(request.theme().typography());
        config.setFeaturedProductId(featuredId);
        config.getProductOrderIds().clear();
        config.getProductOrderIds().addAll(orderIds);
        return configDto(configRepository.save(config));
    }

    @Transactional(readOnly = true)
    public StorefrontDtos.PublicStorefront getPublicStorefront(UUID creatorId) {
        User creator = requireCreator(creatorId);
        StorefrontConfig config = configRepository.findByCreatorUserId(creatorId).orElse(null);
        List<Product> published = productRepository.findAllByUser(creator).stream()
                .filter(product -> product.getStatus() == ProductStatus.PUBLISHED).toList();
        List<Product> ordered = orderProducts(published, config == null ? List.of() : config.getProductOrderIds());
        Set<UUID> visibleIds = ordered.stream().map(Product::getId).collect(java.util.stream.Collectors.toSet());
        UUID featuredId = config == null ? null : config.getFeaturedProductId();
        if (featuredId != null && !visibleIds.contains(featuredId)) featuredId = null;

        return new StorefrontDtos.PublicStorefront(
                config == null ? creatorId.toString() : config.getId().toString(),
                publicCreator(creator),
                featuredId == null ? null : featuredId.toString(),
                ordered.stream().map(StorefrontService::publicProduct).toList(),
                config == null ? DEFAULT_THEME : theme(config)
        );
    }

    private User requireCreator(UUID creatorId) {
        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Storefront not found"));
        boolean creator = user.getRoles().stream().map(Role::getRoleName).anyMatch("CREATOR"::equals);
        if (!creator) throw new ResourceNotFoundException("Storefront not found");
        return user;
    }

    private Map<UUID, Product> ownedProducts(User creator) {
        Map<UUID, Product> products = new LinkedHashMap<>();
        productRepository.findAllByUser(creator).forEach(product -> products.put(product.getId(), product));
        return products;
    }

    private static Product requireOwned(UUID id, Map<UUID, Product> products) {
        Product product = products.get(id);
        if (product == null) throw new AccessDeniedException("Product does not belong to the Creator");
        return product;
    }

    private static List<UUID> parseUniqueIds(List<String> values, String field) {
        List<UUID> ids = values.stream().map(value -> optionalId(value, field)).toList();
        if (ids.contains(null) || new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException(field + " must contain unique Product IDs");
        }
        return ids;
    }

    private static UUID optionalId(String value, String field) {
        if (value == null || value.isBlank()) return null;
        try { return UUID.fromString(value); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException(field + " contains an invalid UUID"); }
    }

    private static List<Product> orderProducts(List<Product> products, List<UUID> configuredOrder) {
        Map<UUID, Product> remaining = new LinkedHashMap<>();
        products.forEach(product -> remaining.put(product.getId(), product));
        List<Product> result = new ArrayList<>();
        configuredOrder.forEach(id -> {
            Product product = remaining.remove(id);
            if (product != null) result.add(product);
        });
        remaining.values().stream().sorted(Comparator
                .comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Product::getId)).forEach(result::add);
        return List.copyOf(result);
    }

    private static StorefrontDtos.Config configDto(StorefrontConfig config) {
        return new StorefrontDtos.Config(config.getId().toString(),
                config.getFeaturedProductId() == null ? null : config.getFeaturedProductId().toString(),
                config.getProductOrderIds().stream().map(UUID::toString).toList(), theme(config), config.getUpdatedAt());
    }

    private static StorefrontDtos.Theme theme(StorefrontConfig config) {
        return new StorefrontDtos.Theme(config.getAppearance(), config.getAccentColor(), config.getTypography());
    }

    private static StorefrontDtos.PublicCreator publicCreator(User creator) {
        List<SocialMediaLinkResponse> links = creator.getSocialLinks().stream().map(link ->
                new SocialMediaLinkResponse(link.getId(), link.getPlatform(), link.getUrl(), link.getCreatedAt())).toList();
        String displayName = (creator.getFirstName() + " " + creator.getLastName()).trim();
        return new StorefrontDtos.PublicCreator(creator.getUserId().toString(), displayName, null,
                creator.getTitle(), creator.getTaglineMission(), creator.getBio(), creator.getWebsite(),
                creator.getPublicEmail(), null, links);
    }

    private static StorefrontDtos.PublicProduct publicProduct(Product product) {
        BigDecimal price = product.getPrice();
        Object publicPrice = price == null || price.signum() <= 0 ? "free" : price;
        return new StorefrontDtos.PublicProduct(product.getId().toString(), product.getName(), product.getDescription(),
                product.getType(), product.getStatus(), publicPrice, product.getImage());
    }
}

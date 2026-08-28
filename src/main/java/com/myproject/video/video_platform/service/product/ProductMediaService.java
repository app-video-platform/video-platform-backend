package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.media.ProductGalleryImageDto;
import com.myproject.video.video_platform.dto.products.media.ProductPromoVideoDto;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.products.ProductMedia;
import com.myproject.video.video_platform.exception.product.ProductMediaException;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.ProductMediaRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.service.digitalocean.SpacesS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
@Slf4j
public class ProductMediaService {
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> VIDEO_TYPES = Set.of("video/mp4", "video/webm");
    private final ProductRepository products;
    private final ProductMediaRepository media;
    private final ProductAuthorizationService authorization;
    private final SpacesS3Service spaces;
    private final String cdnEndpoint;
    private final long imageMaxBytes;
    private final long videoMaxBytes;
    private final int galleryMaxItems;

    public ProductMediaService(ProductRepository products, ProductMediaRepository media,
                               ProductAuthorizationService authorization, SpacesS3Service spaces,
                               @Value("${digitalocean.spaces.cdnEndpointUrl}") String cdnEndpoint,
                               @Value("${app.product-media.image-max-bytes:10485760}") long imageMaxBytes,
                               @Value("${app.product-media.video-max-bytes:104857600}") long videoMaxBytes,
                               @Value("${app.product-media.gallery-max-items:20}") int galleryMaxItems) {
        this.products = products; this.media = media; this.authorization = authorization; this.spaces = spaces;
        this.cdnEndpoint = cdnEndpoint.replaceAll("/+$", "");
        this.imageMaxBytes = imageMaxBytes; this.videoMaxBytes = videoMaxBytes; this.galleryMaxItems = galleryMaxItems;
    }

    @Transactional
    public String replaceThumbnail(UUID productId, InputStream body, long size, String type) {
        Product product = owned(productId); type = validateFile(type, size, IMAGE_TYPES, imageMaxBytes);
        ProductMedia created = upload(product, ProductMedia.Kind.THUMBNAIL, body, size, type, null);
        ProductMedia previous = media.findByProductIdAndKind(productId, ProductMedia.Kind.THUMBNAIL).orElse(null);
        removeReference(previous); media.save(created); product.setImage(created.getCdnUrl()); products.save(product);
        return created.getCdnUrl();
    }

    @Transactional
    public void removeThumbnail(UUID productId) {
        Product product = owned(productId);
        ProductMedia previous = media.findByProductIdAndKind(productId, ProductMedia.Kind.THUMBNAIL).orElse(null);
        product.setImage(null); products.save(product); removeReference(previous);
    }

    @Transactional
    public ProductGalleryImageDto addGallery(UUID productId, InputStream body, long size, String type) {
        Product product = owned(productId); type = validateFile(type, size, IMAGE_TYPES, imageMaxBytes);
        long count = media.countByProductIdAndKind(productId, ProductMedia.Kind.GALLERY_IMAGE);
        if (count >= galleryMaxItems) throw new ProductMediaException("Gallery limit is " + galleryMaxItems + " images");
        return gallery(media.save(upload(product, ProductMedia.Kind.GALLERY_IMAGE, body, size, type, (int) count)));
    }

    @Transactional
    public void removeGallery(UUID productId, UUID imageId) {
        owned(productId);
        ProductMedia item = media.findByIdAndProductIdAndKind(imageId, productId, ProductMedia.Kind.GALLERY_IMAGE)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery image not found: " + imageId));
        removeReference(item);
        List<ProductMedia> remaining = media.findAllByProductIdAndKindOrderByGalleryPositionAsc(productId, ProductMedia.Kind.GALLERY_IMAGE);
        for (int i = 0; i < remaining.size(); i++) remaining.get(i).setGalleryPosition(i);
        media.saveAll(remaining);
    }

    @Transactional
    public List<ProductGalleryImageDto> reorder(UUID productId, List<UUID> ids) {
        owned(productId);
        List<ProductMedia> current = media.findAllByProductIdAndKindOrderByGalleryPositionAsc(productId, ProductMedia.Kind.GALLERY_IMAGE);
        if (ids == null || ids.size() != current.size() || new HashSet<>(ids).size() != ids.size()
                || !new HashSet<>(ids).equals(current.stream().map(ProductMedia::getId).collect(java.util.stream.Collectors.toSet()))) {
            throw new ProductMediaException("Gallery order must contain every gallery image exactly once");
        }
        Map<UUID, ProductMedia> byId = new HashMap<>(); current.forEach(item -> byId.put(item.getId(), item));
        for (int i = 0; i < ids.size(); i++) byId.get(ids.get(i)).setGalleryPosition(i);
        media.saveAll(current);
        return ids.stream().map(byId::get).map(this::gallery).toList();
    }

    @Transactional
    public ProductPromoVideoDto replacePromo(UUID productId, InputStream body, long size, String type) {
        Product product = owned(productId); type = validateFile(type, size, VIDEO_TYPES, videoMaxBytes);
        ProductMedia created = upload(product, ProductMedia.Kind.PROMO_VIDEO, body, size, type, null);
        ProductMedia previous = media.findByProductIdAndKind(productId, ProductMedia.Kind.PROMO_VIDEO).orElse(null);
        removeReference(previous); media.save(created); return promo(created);
    }

    @Transactional
    public void removePromo(UUID productId) {
        owned(productId);
        removeReference(media.findByProductIdAndKind(productId, ProductMedia.Kind.PROMO_VIDEO).orElse(null));
    }

    @Transactional
    public void removeAll(UUID productId) {
        List<ProductMedia> all = media.findAllByProductId(productId);
        media.deleteAllByProductId(productId); media.flush();
        all.forEach(this::deleteObjectQuietly);
    }

    @Transactional(readOnly = true)
    public void enrich(UUID productId, AbstractProductResponseDto response) {
        response.setImageUrl(response.getImageUrl() != null ? response.getImageUrl() : products.findById(productId).map(Product::getImage).orElse(null));
        response.setGalleryImages(media.findAllByProductIdAndKindOrderByGalleryPositionAsc(productId, ProductMedia.Kind.GALLERY_IMAGE)
                .stream().map(this::gallery).toList());
        response.setPromoVideo(media.findByProductIdAndKind(productId, ProductMedia.Kind.PROMO_VIDEO).map(this::promo).orElse(null));
    }

    private Product owned(UUID id) {
        Product product = products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        authorization.requireOwnerOrAdmin(product); return product;
    }

    private ProductMedia upload(Product product, ProductMedia.Kind kind, InputStream body, long size, String type, Integer position) {
        String extension = switch (type.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "jpg"; case "image/png" -> "png"; case "image/webp" -> "webp";
            case "image/gif" -> "gif"; case "video/mp4" -> "mp4"; case "video/webm" -> "webm";
            default -> throw new ProductMediaException("Unsupported media type");
        };
        String filename = UUID.randomUUID() + "." + extension;
        String key = "products/" + product.getUser().getUserId() + "/" + product.getId() + "/" + kind.name().toLowerCase(Locale.ROOT) + "/" + filename;
        try { spaces.upload(key, body, size, type); }
        catch (RuntimeException ex) { throw new ProductMediaException("Product media upload failed", ex); }
        ProductMedia item = new ProductMedia(); item.setProductId(product.getId()); item.setKind(kind); item.setObjectKey(key);
        item.setCdnUrl(cdnEndpoint + "/" + key); item.setFileName(filename); item.setMimeType(type); item.setSize(size);
        item.setGalleryPosition(position == null ? -1 : position); item.setStatus(ProductMedia.Status.READY); return item;
    }

    private static String validateFile(String type, long size, Set<String> allowed, long maximum) {
        String normalized = type == null ? "" : type.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (!allowed.contains(normalized)) throw new ProductMediaException("Unsupported media type: " + type);
        if (size <= 0 || size > maximum) throw new ProductMediaException("Media size must be between 1 and " + maximum + " bytes");
        return normalized;
    }

    private void removeReference(ProductMedia item) {
        if (item == null) return; media.delete(item); media.flush(); deleteObjectQuietly(item);
    }
    private void deleteObjectQuietly(ProductMedia item) {
        try { spaces.delete(item.getObjectKey()); }
        catch (RuntimeException ex) { log.warn("Failed to remove orphaned Product media object {}", item.getObjectKey(), ex); }
    }
    private ProductGalleryImageDto gallery(ProductMedia item) {
        return new ProductGalleryImageDto(item.getId(), item.getCdnUrl(), item.getFileName(), item.getMimeType(), item.getSize(),
                item.getGalleryPosition(), null, item.getStatus().name());
    }
    private ProductPromoVideoDto promo(ProductMedia item) {
        return new ProductPromoVideoDto(item.getId(), item.getCdnUrl(), item.getFileName(), item.getMimeType(), item.getSize(), item.getStatus().name(), null);
    }
}

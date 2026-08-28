package com.myproject.video.video_platform.repository.products;

import com.myproject.video.video_platform.entity.products.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductMediaRepository extends JpaRepository<ProductMedia, UUID> {
    Optional<ProductMedia> findByProductIdAndKind(UUID productId, ProductMedia.Kind kind);
    Optional<ProductMedia> findByIdAndProductIdAndKind(UUID id, UUID productId, ProductMedia.Kind kind);
    List<ProductMedia> findAllByProductIdAndKindOrderByGalleryPositionAsc(UUID productId, ProductMedia.Kind kind);
    List<ProductMedia> findAllByProductId(UUID productId);
    long countByProductIdAndKind(UUID productId, ProductMedia.Kind kind);
    void deleteAllByProductId(UUID productId);
}

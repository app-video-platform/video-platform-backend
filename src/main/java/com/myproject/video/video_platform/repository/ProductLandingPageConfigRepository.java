package com.myproject.video.video_platform.repository;

import com.myproject.video.video_platform.entity.ProductLandingPageConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductLandingPageConfigRepository extends JpaRepository<ProductLandingPageConfig, UUID> {
    Optional<ProductLandingPageConfig> findByProductId(UUID productId);
    void deleteByProductId(UUID productId);
}

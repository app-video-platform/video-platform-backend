package com.myproject.video.video_platform.repository;

import com.myproject.video.video_platform.entity.StorefrontConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StorefrontConfigRepository extends JpaRepository<StorefrontConfig, UUID> {
    Optional<StorefrontConfig> findByCreatorUserId(UUID creatorId);
}

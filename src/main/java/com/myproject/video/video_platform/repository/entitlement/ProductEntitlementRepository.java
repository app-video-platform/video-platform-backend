package com.myproject.video.video_platform.repository.entitlement;

import com.myproject.video.video_platform.common.enums.entitlement.EntitlementStatus;
import com.myproject.video.video_platform.entity.entitlement.ProductEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductEntitlementRepository extends JpaRepository<ProductEntitlement, UUID> {

    Optional<ProductEntitlement> findByUserUserIdAndProductId(UUID userId, UUID productId);

    boolean existsByUserUserIdAndProductIdAndStatus(
            UUID userId,
            UUID productId,
            EntitlementStatus status
    );

    List<ProductEntitlement> findAllByUserUserIdAndStatusOrderByCreatedAtDesc(
            UUID userId,
            EntitlementStatus status
    );

    void deleteAllByProductId(UUID productId);
}

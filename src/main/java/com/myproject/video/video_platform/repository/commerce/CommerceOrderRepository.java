package com.myproject.video.video_platform.repository.commerce;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommerceOrderRepository extends JpaRepository<CommerceOrder, UUID> {

    @EntityGraph(attributePaths = {"items", "buyer", "creator"})
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CommerceOrder> findByBuyerUserIdAndIdempotencyKey(UUID buyerId, String idempotencyKey);

    @EntityGraph(attributePaths = {"items", "buyer", "creator"})
    @Query("select o from CommerceOrder o where o.id = :orderId")
    Optional<CommerceOrder> findDetailedById(@Param("orderId") UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"items", "buyer", "creator"})
    @Query("select o from CommerceOrder o where o.id = :orderId")
    Optional<CommerceOrder> findDetailedByIdForUpdate(@Param("orderId") UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<CommerceOrder> findAllByStatusAndExpiresAtBefore(
            CommerceOrderStatus status,
            Instant expiresAt
    );

    @Query("""
            select case when count(o) > 0 then true else false end
            from CommerceOrder o join o.items i
            where i.productId = :productId
              and o.status = :pendingStatus
              and o.expiresAt > :now
            """)
    boolean existsLivePendingOrderForProduct(
            @Param("productId") UUID productId,
            @Param("pendingStatus") CommerceOrderStatus pendingStatus,
            @Param("now") Instant now
    );
}

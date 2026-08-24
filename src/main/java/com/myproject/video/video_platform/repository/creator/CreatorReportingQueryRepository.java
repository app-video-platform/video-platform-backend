package com.myproject.video.video_platform.repository.creator;

import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.commerce.CommercePaymentAttempt;
import com.myproject.video.video_platform.entity.entitlement.ProductEntitlement;
import com.myproject.video.video_platform.entity.products.Product;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CreatorReportingQueryRepository {
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public ReportingData load(UUID creatorId) {
        List<CommerceOrder> orders = entityManager.createQuery("""
                select distinct o from CommerceOrder o
                join fetch o.buyer
                left join fetch o.items
                where o.creator.userId = :creatorId
                """, CommerceOrder.class).setParameter("creatorId", creatorId).getResultList();

        List<Product> products = entityManager.createQuery("""
                select p from Product p where p.user.userId = :creatorId
                """, Product.class).setParameter("creatorId", creatorId).getResultList();
        List<UUID> productIds = products.stream().map(Product::getId).toList();

        List<ProductEntitlement> entitlements = productIds.isEmpty() ? List.of() : entityManager.createQuery("""
                select e from ProductEntitlement e join fetch e.user
                where e.productId in :productIds
                """, ProductEntitlement.class).setParameter("productIds", productIds).getResultList();

        List<UUID> orderIds = orders.stream().map(CommerceOrder::getId).toList();
        List<CommercePaymentAttempt> attempts = orderIds.isEmpty() ? List.of() : entityManager.createQuery("""
                select a from CommercePaymentAttempt a where a.order.id in :orderIds
                """, CommercePaymentAttempt.class).setParameter("orderIds", orderIds).getResultList();

        return new ReportingData(
                List.copyOf(orders), List.copyOf(products), List.copyOf(entitlements),
                attempts.stream().collect(Collectors.toUnmodifiableMap(a -> a.getOrder().getId(), Function.identity()))
        );
    }

    public record ReportingData(
            List<CommerceOrder> orders,
            List<Product> products,
            List<ProductEntitlement> entitlements,
            Map<UUID, CommercePaymentAttempt> attemptsByOrderId
    ) {
    }
}

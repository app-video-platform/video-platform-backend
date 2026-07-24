package com.myproject.video.video_platform.service.entitlement;

import com.myproject.video.video_platform.common.converter.product.ProductConverter;
import com.myproject.video.video_platform.common.enums.entitlement.EntitlementSource;
import com.myproject.video.video_platform.common.enums.entitlement.EntitlementStatus;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.entitlement.ProductEntitlementDto;
import com.myproject.video.video_platform.entity.entitlement.ProductEntitlement;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.PaymentRequiredException;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.entitlement.ProductEntitlementRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductEntitlementService {

    private final ProductEntitlementRepository entitlementRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductConverter productConverter;
    private final CurrentUserService currentUserService;

    @Transactional
    public ProductEntitlementDto enrollInFreeProduct(UUID productId) {
        UUID userId = currentUserService.getCurrentUserId();
        Product product = requireProduct(productId);

        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new AccessDeniedException("Only published products can be enrolled in");
        }
        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            throw new PaymentRequiredException(
                    "This product requires payment before access can be granted"
            );
        }

        ProductEntitlement entitlement = entitlementRepository
                .findByUserUserIdAndProductId(userId, productId)
                .orElseGet(ProductEntitlement::new);

        if (entitlement.getId() == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            entitlement.setUser(user);
            entitlement.setProductId(productId);
            entitlement.setProductType(product.getType());
            entitlement.setSource(EntitlementSource.FREE_ENROLLMENT);
        }

        entitlement.setStatus(EntitlementStatus.ACTIVE);
        entitlement.setRevokedAt(null);
        return toDto(entitlementRepository.save(entitlement), product);
    }

    @Transactional(readOnly = true)
    public List<ProductEntitlementDto> getCurrentUserLibrary(ProductType type) {
        UUID userId = currentUserService.getCurrentUserId();
        return entitlementRepository
                .findAllByUserUserIdAndStatusOrderByCreatedAtDesc(userId, EntitlementStatus.ACTIVE)
                .stream()
                .filter(entitlement -> type == null || entitlement.getProductType() == type)
                .map(entitlement -> productRepository.findById(entitlement.getProductId())
                        .map(product -> toDto(entitlement, product))
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasActiveEntitlement(UUID userId, UUID productId) {
        return entitlementRepository.existsByUserUserIdAndProductIdAndStatus(
                userId,
                productId,
                EntitlementStatus.ACTIVE
        );
    }

    @Transactional
    public ProductEntitlement grant(
            UUID userId,
            Product product,
            EntitlementSource source
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        ProductEntitlement entitlement = entitlementRepository
                .findByUserUserIdAndProductId(userId, product.getId())
                .orElseGet(ProductEntitlement::new);
        entitlement.setUser(user);
        entitlement.setProductId(product.getId());
        entitlement.setProductType(product.getType());
        entitlement.setSource(source);
        entitlement.setStatus(EntitlementStatus.ACTIVE);
        entitlement.setRevokedAt(null);
        return entitlementRepository.save(entitlement);
    }

    @Transactional
    public void revoke(UUID userId, UUID productId) {
        entitlementRepository.findByUserUserIdAndProductId(userId, productId)
                .ifPresent(entitlement -> {
                    entitlement.setStatus(EntitlementStatus.REVOKED);
                    entitlement.setRevokedAt(Instant.now());
                    entitlementRepository.save(entitlement);
                });
    }

    private Product requireProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private ProductEntitlementDto toDto(ProductEntitlement entitlement, Product product) {
        return new ProductEntitlementDto(
                entitlement.getId(),
                entitlement.getSource(),
                entitlement.getCreatedAt(),
                productConverter.mapProductMinimisedToResponse(product)
        );
    }
}

package com.myproject.video.video_platform.service.entitlement;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductResponseDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductResponseDto;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductContentAccessService {

    private final ProductEntitlementService entitlementService;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public boolean canAccessContent(Product product) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = authenticatedUserId(authentication);
        if (userId == null) {
            return false;
        }
        if (hasAdminRole(authentication) || product.getUser().getUserId().equals(userId)) {
            return true;
        }
        return entitlementService.hasActiveEntitlement(userId, product.getId());
    }

    @Transactional(readOnly = true)
    public boolean canAccessContent(UUID productId) {
        return canAccessContent(requireProduct(productId));
    }

    @Transactional(readOnly = true)
    public void requireContentAccess(Product product) {
        if (!canAccessContent(product)) {
            throw new AccessDeniedException("Product access is required");
        }
    }

    @Transactional(readOnly = true)
    public AbstractProductResponseDto protectProductResponse(AbstractProductResponseDto response) {
        Product product = requireProduct(response.getId());
        if (canAccessContent(product)) {
            removePermanentDownloadUrls(response);
            return response;
        }
        if (product.getStatus() != ProductStatus.PUBLISHED) {
            throw new AccessDeniedException("This product is not publicly available");
        }
        removeProtectedContent(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<AbstractProductResponseDto> protectProductResponses(
            List<AbstractProductResponseDto> responses
    ) {
        return responses.stream()
                .map(response -> {
                    try {
                        return protectProductResponse(response);
                    } catch (AccessDeniedException ignored) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private void removeProtectedContent(AbstractProductResponseDto response) {
        if (response instanceof CourseProductResponseDto course
                && course.getDetails() != null
                && course.getDetails().getSections() != null) {
            course.getDetails().getSections().forEach(section -> {
                if (section.getLessons() != null) {
                    section.getLessons().forEach(lesson -> {
                        lesson.setVideoUrl(null);
                        lesson.setContent(null);
                    });
                }
            });
        }
        removePermanentDownloadUrls(response);
    }

    private void removePermanentDownloadUrls(AbstractProductResponseDto response) {
        if (response instanceof DownloadProductResponseDto download
                && download.getDetails() != null
                && download.getDetails().getSections() != null) {
            download.getDetails().getSections().forEach(section -> {
                if (section.getFiles() != null) {
                    section.getFiles().forEach(file -> file.setUrl(null));
                }
            });
        }
    }

    private Product requireProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private UUID authenticatedUserId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        try {
            return currentUserService.getCurrentUserId();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> UserRole.ADMIN.authority().equals(authority.getAuthority()));
    }
}

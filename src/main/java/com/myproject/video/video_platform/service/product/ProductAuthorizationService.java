package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductAuthorizationService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public ProductAuthorizationService(CurrentUserService currentUserService,
                                       UserRepository userRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    public boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(authority -> UserRole.ADMIN.authority().equals(authority.getAuthority()));
    }

    public UUID getCurrentUserId() {
        return currentUserService.getCurrentUserId();
    }

    public void requireOwnerOrAdmin(Product product) {
        requireOwnerOrAdmin(product.getUser().getUserId());
    }

    public void requireOwnerOrAdmin(UUID ownerId) {
        if (isCurrentUserAdmin()) {
            return;
        }

        UUID currentUserId = currentUserService.getCurrentUserId();
        if (!ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("You don’t own this product.");
        }
    }

    public User resolveOwnerForCreate(AbstractProductRequestDto dto) {
        if (isCurrentUserAdmin()) {
            if (dto.getUserId() == null || dto.getUserId().isBlank()) {
                throw new AccessDeniedException("Admin product creation requires a creator owner");
            }

            User owner = getUser(UUID.fromString(dto.getUserId()));
            if (!hasSingleRole(owner, UserRole.CREATOR)) {
                throw new AccessDeniedException("Product owner must have CREATOR role");
            }
            return owner;
        }

        UUID currentUserId = currentUserService.getCurrentUserId();
        User owner = getUser(currentUserId);
        dto.setUserId(currentUserId.toString());
        return owner;
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    private boolean hasSingleRole(User user, UserRole role) {
        return user.getRoles().size() == 1
                && user.getRoles().stream()
                .map(Role::getRoleName)
                .anyMatch(role.name()::equals);
    }
}

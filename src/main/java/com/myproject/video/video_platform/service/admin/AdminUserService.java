package com.myproject.video.video_platform.service.admin;

import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.admin.AdminUserDto;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.service.security.RefreshTokenService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final AdminAuditService auditService;

    public AdminUserService(UserRepository userRepository,
                            RoleRepository roleRepository,
                            RefreshTokenService refreshTokenService,
                            AdminAuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDto> searchUsers(String search, UserRole role, Pageable pageable) {
        return userRepository.findAll(userSpec(search, role), pageable).map(this::toDto);
    }

    @Transactional
    public AdminUserDto updateRole(String userId, UserRole nextRole) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String previousRole = singleRoleName(user);
        if (UserRole.ADMIN.name().equals(previousRole)
                && nextRole != UserRole.ADMIN
                && userRepository.countByRoleName(UserRole.ADMIN.name()) <= 1) {
            throw new AccessDeniedException("Cannot remove the last admin");
        }

        Role role = roleRepository.findByRoleName(nextRole.name());
        if (role == null) {
            throw new IllegalStateException("Role " + nextRole.name() + " is missing from the database");
        }

        user.setRoles(new HashSet<>(Set.of(role)));
        User saved = userRepository.save(user);
        refreshTokenService.deleteRefreshTokensForUser(saved.getEmail());

        auditService.recordCurrentAdminAction(
                "ROLE_CHANGE",
                "USER",
                saved.getUserId().toString(),
                previousRole,
                nextRole.name()
        );

        return toDto(saved);
    }

    private Specification<User> userSpec(String search, UserRole role) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), term),
                        cb.like(cb.lower(root.get("lastName")), term),
                        cb.like(cb.lower(root.get("email")), term)
                ));
            }
            if (role != null) {
                Join<User, Role> roleJoin = root.join("roles");
                predicates.add(cb.equal(roleJoin.get("roleName"), role.name()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private AdminUserDto toDto(User user) {
        return AdminUserDto.builder()
                .id(user.getUserId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getRoleName).toList())
                .enabled(user.isEnabled())
                .authProvider(user.getAuthProvider())
                .onboardingCompleted(user.isOnboardingcompleted())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String singleRoleName(User user) {
        return user.getRoles().stream()
                .findFirst()
                .map(Role::getRoleName)
                .orElse(null);
    }
}

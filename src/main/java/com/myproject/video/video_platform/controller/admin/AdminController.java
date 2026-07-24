package com.myproject.video.video_platform.controller.admin;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.admin.AdminAuditLogDto;
import com.myproject.video.video_platform.dto.admin.AdminRoleUpdateRequest;
import com.myproject.video.video_platform.dto.admin.AdminUserDto;
import com.myproject.video.video_platform.dto.products.ProductMinimised;
import com.myproject.video.video_platform.service.admin.AdminAuditService;
import com.myproject.video.video_platform.service.admin.AdminProductService;
import com.myproject.video.video_platform.service.admin.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminUserService adminUserService;
    private final AdminProductService adminProductService;
    private final AdminAuditService adminAuditService;

    public AdminController(AdminUserService adminUserService,
                           AdminProductService adminProductService,
                           AdminAuditService adminAuditService) {
        this.adminUserService = adminUserService;
        this.adminProductService = adminProductService;
        this.adminAuditService = adminAuditService;
    }

    @GetMapping("/users")
    public Page<AdminUserDto> users(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return adminUserService.searchUsers(search, role, pageable);
    }

    @PatchMapping("/users/{userId}/role")
    public AdminUserDto updateRole(@PathVariable String userId,
                                   @Valid @RequestBody AdminRoleUpdateRequest request) {
        return adminUserService.updateRole(userId, request.getRole());
    }

    @GetMapping("/products")
    public Page<ProductMinimised> products(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return adminProductService.searchProducts(search, ownerId, type, status, pageable);
    }

    @GetMapping("/audit")
    public Page<AdminAuditLogDto> audit(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return adminAuditService.search(actorId, targetType, targetId, action, pageable);
    }
}

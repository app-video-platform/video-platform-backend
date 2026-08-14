package com.myproject.video.video_platform.controller.entitlement;

import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.entitlement.ProductAccessDto;
import com.myproject.video.video_platform.dto.entitlement.ProductEntitlementDto;
import com.myproject.video.video_platform.dto.entitlement.ProductFileDownloadDto;
import com.myproject.video.video_platform.service.entitlement.ProductContentAccessService;
import com.myproject.video.video_platform.service.entitlement.ProductEntitlementService;
import com.myproject.video.video_platform.service.entitlement.ProductFileAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/entitlements")
@RequiredArgsConstructor
public class ProductEntitlementController {

    private final ProductEntitlementService entitlementService;
    private final ProductContentAccessService contentAccessService;
    private final ProductFileAccessService fileAccessService;

    @PostMapping("/products/{productId}/enroll")
    public ResponseEntity<ProductEntitlementDto> enroll(
            @PathVariable UUID productId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entitlementService.enrollInFreeProduct(productId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ProductEntitlementDto>> library(
            @RequestParam(required = false) ProductType type
    ) {
        return ResponseEntity.ok(entitlementService.getCurrentUserLibrary(type));
    }

    @GetMapping("/products/{productId}/access")
    public ResponseEntity<ProductAccessDto> access(@PathVariable UUID productId) {
        boolean hasAccess = contentAccessService.canAccessContent(productId);
        return ResponseEntity.ok(new ProductAccessDto(
                hasAccess,
                hasAccess ? "ACCESS_GRANTED" : "ENTITLEMENT_REQUIRED"
        ));
    }

    @GetMapping("/products/{productId}/files/{fileId}/download")
    public ResponseEntity<ProductFileDownloadDto> download(
            @PathVariable UUID productId,
            @PathVariable UUID fileId
    ) {
        return ResponseEntity.ok(new ProductFileDownloadDto(
                fileAccessService.createDownloadUrl(productId, fileId)
        ));
    }
}

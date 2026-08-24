package com.myproject.video.video_platform.controller.creator;

import com.myproject.video.video_platform.controller.docs.creator.ProductLandingPageApiDoc;
import com.myproject.video.video_platform.dto.products.landing.ProductLandingPageDtos;
import com.myproject.video.video_platform.service.ProductLandingPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/creator/products/{productId}/landing-page")
@PreAuthorize("hasAnyRole('CREATOR','ADMIN')")
@RequiredArgsConstructor
public class ProductLandingPageController implements ProductLandingPageApiDoc {
    private final ProductLandingPageService service;

    @Override @GetMapping
    public ResponseEntity<ProductLandingPageDtos.Config> getConfig(@PathVariable UUID productId) {
        return ResponseEntity.ok(service.getForManager(productId));
    }

    @Override @PatchMapping
    public ResponseEntity<ProductLandingPageDtos.Config> updateConfig(@PathVariable UUID productId,
            @Valid @RequestBody ProductLandingPageDtos.UpdateRequest request) {
        return ResponseEntity.ok(service.update(productId, request));
    }
}

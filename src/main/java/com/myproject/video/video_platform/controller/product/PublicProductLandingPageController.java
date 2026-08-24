package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.controller.docs.product.PublicProductLandingPageApiDoc;
import com.myproject.video.video_platform.dto.products.landing.ProductLandingPageDtos;
import com.myproject.video.video_platform.service.ProductLandingPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/landing-page")
@RequiredArgsConstructor
public class PublicProductLandingPageController implements PublicProductLandingPageApiDoc {
    private final ProductLandingPageService service;

    @Override @GetMapping
    public ResponseEntity<ProductLandingPageDtos.Config> getPublicConfig(@PathVariable UUID productId) {
        return ResponseEntity.ok(service.getPublic(productId));
    }
}

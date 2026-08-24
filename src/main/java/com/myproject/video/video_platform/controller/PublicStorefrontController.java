package com.myproject.video.video_platform.controller;

import com.myproject.video.video_platform.controller.docs.PublicStorefrontApiDoc;
import com.myproject.video.video_platform.dto.storefront.StorefrontDtos;
import com.myproject.video.video_platform.service.StorefrontService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/storefronts")
@RequiredArgsConstructor
public class PublicStorefrontController implements PublicStorefrontApiDoc {
    private final StorefrontService storefrontService;

    @Override @GetMapping("/{creatorId}")
    public ResponseEntity<StorefrontDtos.PublicStorefront> getStorefront(@PathVariable UUID creatorId) {
        return ResponseEntity.ok(storefrontService.getPublicStorefront(creatorId));
    }
}

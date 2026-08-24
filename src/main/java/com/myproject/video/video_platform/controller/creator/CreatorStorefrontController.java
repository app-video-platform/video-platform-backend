package com.myproject.video.video_platform.controller.creator;

import com.myproject.video.video_platform.controller.docs.creator.CreatorStorefrontApiDoc;
import com.myproject.video.video_platform.dto.storefront.StorefrontDtos;
import com.myproject.video.video_platform.service.StorefrontService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/creator/storefront")
@PreAuthorize("hasRole('CREATOR')")
@RequiredArgsConstructor
public class CreatorStorefrontController implements CreatorStorefrontApiDoc {
    private final StorefrontService storefrontService;

    @Override @GetMapping
    public ResponseEntity<StorefrontDtos.Config> getConfig() {
        return ResponseEntity.ok(storefrontService.getCreatorConfig());
    }

    @Override @PatchMapping
    public ResponseEntity<StorefrontDtos.Config> updateConfig(@Valid @RequestBody StorefrontDtos.UpdateRequest request) {
        return ResponseEntity.ok(storefrontService.updateCreatorConfig(request));
    }
}

package com.myproject.video.video_platform.controller.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.myproject.video.video_platform.dto.products.membership.MembershipDtos;
import com.myproject.video.video_platform.service.product.membership.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/products/{productId}/membership")
@PreAuthorize("hasAnyRole('CREATOR','ADMIN')")
@Tag(name = "Membership authoring", description = "Creator/Admin authoring for Membership config, metadata-only content, and feeds.")
@RequiredArgsConstructor
public class MembershipController {
    private final MembershipService membershipService;

    @GetMapping
    @Operation(summary = "Get the complete Membership authoring aggregate")
    public ResponseEntity<MembershipDtos.Aggregate> getAggregate(@PathVariable UUID productId) {
        return ResponseEntity.ok(membershipService.getAggregate(productId));
    }

    @PatchMapping
    @Operation(summary = "Update Membership configuration")
    public ResponseEntity<MembershipDtos.Aggregate> updateConfig(
            @PathVariable UUID productId,
            @RequestBody(required = false) JsonNode request
    ) {
        return ResponseEntity.ok(membershipService.updateConfig(productId, request));
    }

    @PostMapping("/content")
    @Operation(summary = "Create native Membership content and its feed entry")
    public ResponseEntity<MembershipDtos.Content> createContent(
            @PathVariable UUID productId,
            @RequestBody JsonNode request
    ) {
        return ResponseEntity.status(201).body(membershipService.createContent(productId, request));
    }

    @PatchMapping("/content/{contentId}")
    @Operation(summary = "Partially update native Membership content")
    public ResponseEntity<MembershipDtos.Content> updateContent(
            @PathVariable UUID productId,
            @PathVariable UUID contentId,
            @RequestBody JsonNode request
    ) {
        return ResponseEntity.ok(membershipService.updateContent(productId, contentId, request));
    }

    @DeleteMapping("/content/{contentId}")
    @Operation(summary = "Delete native Membership content and its feed entry")
    public ResponseEntity<Void> deleteContent(
            @PathVariable UUID productId,
            @PathVariable UUID contentId
    ) {
        membershipService.deleteContent(productId, contentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/feed")
    @Operation(summary = "Transactionally replace the Membership feed")
    public ResponseEntity<MembershipDtos.Aggregate> replaceFeed(
            @PathVariable UUID productId,
            @RequestBody JsonNode request
    ) {
        return ResponseEntity.ok(membershipService.replaceFeed(productId, request));
    }
}

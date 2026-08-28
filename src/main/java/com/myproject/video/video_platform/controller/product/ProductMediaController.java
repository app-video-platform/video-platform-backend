package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.dto.products.media.*;
import com.myproject.video.video_platform.service.product.ProductMediaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CREATOR','ADMIN')")
@Tag(name = "Product Media", description = "Owner/Admin raw uploads proxied to DigitalOcean Spaces.")
public class ProductMediaController {
    private final ProductMediaService service;

    @PostMapping("/image")
    @Operation(summary = "Replace a Product thumbnail", description = "Accepts a raw JPEG, PNG, WebP, or GIF body up to the configured image limit.")
    public ResponseEntity<String> uploadThumbnail(@RequestParam UUID productId, HttpServletRequest request) throws IOException {
        return ResponseEntity.ok(service.replaceThumbnail(productId, request.getInputStream(), request.getContentLengthLong(), request.getContentType()));
    }
    @DeleteMapping("/image")
    @Operation(summary = "Remove a Product thumbnail")
    public ResponseEntity<Void> deleteThumbnail(@RequestParam UUID productId) { service.removeThumbnail(productId); return ResponseEntity.noContent().build(); }
    @PostMapping("/{id}/media/gallery")
    @Operation(summary = "Add a Product gallery image")
    public ProductGalleryImageDto uploadGallery(@PathVariable UUID id, HttpServletRequest request) throws IOException {
        return service.addGallery(id, request.getInputStream(), request.getContentLengthLong(), request.getContentType());
    }
    @DeleteMapping("/{id}/media/gallery/{imageId}")
    @Operation(summary = "Remove a Product gallery image")
    public ResponseEntity<Void> deleteGallery(@PathVariable UUID id, @PathVariable UUID imageId) { service.removeGallery(id, imageId); return ResponseEntity.noContent().build(); }
    @PutMapping("/{id}/media/gallery/order")
    @Operation(summary = "Replace the complete gallery order", description = "imageIds must contain every current gallery image exactly once.")
    public List<ProductGalleryImageDto> reorder(@PathVariable UUID id, @RequestBody GalleryOrderRequest request) { return service.reorder(id, request.getImageIds()); }
    @PostMapping("/{id}/media/promo-video")
    @Operation(summary = "Replace a Product promotional video", description = "Accepts a raw MP4 or WebM body up to the configured video limit.")
    public ProductPromoVideoDto uploadPromo(@PathVariable UUID id, HttpServletRequest request) throws IOException {
        return service.replacePromo(id, request.getInputStream(), request.getContentLengthLong(), request.getContentType());
    }
    @DeleteMapping("/{id}/media/promo-video")
    @Operation(summary = "Remove a Product promotional video")
    public ResponseEntity<Void> deletePromo(@PathVariable UUID id) { service.removePromo(id); return ResponseEntity.noContent().build(); }
}

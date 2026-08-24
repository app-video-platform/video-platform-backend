package com.myproject.video.video_platform.controller.docs.product;

import com.myproject.video.video_platform.dto.products.landing.ProductLandingPageDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface PublicProductLandingPageApiDoc {
    @Operation(summary = "Get a published Product's public Landing Page configuration")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Saved configuration or public defaults"), @ApiResponse(responseCode = "404", description = "Published Product not found")})
    ResponseEntity<ProductLandingPageDtos.Config> getPublicConfig(UUID productId);
}

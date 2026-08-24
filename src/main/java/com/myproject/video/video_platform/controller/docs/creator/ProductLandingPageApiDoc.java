package com.myproject.video.video_platform.controller.docs.creator;

import com.myproject.video.video_platform.dto.products.landing.ProductLandingPageDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface ProductLandingPageApiDoc {
    @Operation(summary = "Get Product Landing Page configuration for management")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Saved configuration or defaults"), @ApiResponse(responseCode = "403", description = "Creator ownership or Admin role required"), @ApiResponse(responseCode = "404", description = "Product not found")})
    ResponseEntity<ProductLandingPageDtos.Config> getConfig(UUID productId);
    @Operation(summary = "Replace Product Landing Page configuration")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Configuration saved"), @ApiResponse(responseCode = "400", description = "Invalid sections or layout"), @ApiResponse(responseCode = "403", description = "Creator ownership or Admin role required")})
    ResponseEntity<ProductLandingPageDtos.Config> updateConfig(UUID productId, ProductLandingPageDtos.UpdateRequest request);
}

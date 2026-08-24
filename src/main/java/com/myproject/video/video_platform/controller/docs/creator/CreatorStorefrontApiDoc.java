package com.myproject.video.video_platform.controller.docs.creator;

import com.myproject.video.video_platform.dto.storefront.StorefrontDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface CreatorStorefrontApiDoc {
    @Operation(summary = "Get Creator Storefront configuration")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Saved configuration or defaults"), @ApiResponse(responseCode = "403", description = "Creator role required")})
    ResponseEntity<StorefrontDtos.Config> getConfig();

    @Operation(summary = "Replace Creator Storefront configuration")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Configuration saved"), @ApiResponse(responseCode = "400", description = "Invalid theme or Product references"), @ApiResponse(responseCode = "403", description = "Creator role or Product ownership required")})
    ResponseEntity<StorefrontDtos.Config> updateConfig(StorefrontDtos.UpdateRequest request);
}

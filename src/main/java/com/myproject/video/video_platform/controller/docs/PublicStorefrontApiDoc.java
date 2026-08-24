package com.myproject.video.video_platform.controller.docs;

import com.myproject.video.video_platform.dto.storefront.StorefrontDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface PublicStorefrontApiDoc {
    @Operation(summary = "Get a Creator's public Storefront", description = "Returns public profile fields and published Products only.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Public Storefront returned"), @ApiResponse(responseCode = "404", description = "Creator Storefront not found")})
    ResponseEntity<StorefrontDtos.PublicStorefront> getStorefront(UUID creatorId);
}

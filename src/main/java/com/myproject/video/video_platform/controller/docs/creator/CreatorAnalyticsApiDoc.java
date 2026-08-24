package com.myproject.video.video_platform.controller.docs.creator;

import com.myproject.video.video_platform.dto.creator.analytics.CreatorAnalyticsDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface CreatorAnalyticsApiDoc {
    @Operation(summary = "Get Creator Analytics overview", description = "Returns UTC-period performance, Product ranking, customer growth, and payment health from Commerce and entitlements.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Overview returned, including valid empty datasets"), @ApiResponse(responseCode = "400", description = "Invalid period"), @ApiResponse(responseCode = "403", description = "Creator role required")})
    ResponseEntity<CreatorAnalyticsDtos.Overview> overview(String period);
}

package com.myproject.video.video_platform.controller.docs.creator;

import com.myproject.video.video_platform.dto.creator.dashboard.CreatorDashboardDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface CreatorDashboardApiDoc {
    @Operation(summary = "Get Creator Dashboard summary", description = "Returns a 30-day Creator reporting summary, recent activity, top Products, and actionable data-quality items.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Dashboard returned, including valid empty datasets"), @ApiResponse(responseCode = "403", description = "Creator role required")})
    ResponseEntity<CreatorDashboardDtos.Summary> summary();
}

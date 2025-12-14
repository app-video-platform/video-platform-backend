package com.myproject.video.video_platform.controller.docs.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface SystemApiDoc {

    @Operation(
            summary = "Read root greeting",
            description = "Returns a simple greeting so monitoring tools can confirm the service is online."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Service responded",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
    )
    String home();

    @Operation(
            summary = "Exercise log sink",
            description = "Lightweight endpoint used during deployments to verify logging and routing."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Test message emitted",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
    )
    String testEndpoint();
}


package com.myproject.video.video_platform.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Tag(name = "System", description = "Utility endpoints for health checks and diagnostics.")
public class HomeController {

    @GetMapping("/")
    @Operation(summary = "Read root greeting", description = "Returns a simple greeting so monitoring tools can confirm the service is online.")
    @ApiResponse(responseCode = "200", description = "Service responded",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    public String home() {
        return "Hello there! :)";
    }

    @GetMapping("/testEndpoint")
    @Operation(summary = "Exercise log sink", description = "Lightweight endpoint used during deployments to verify logging and routing.")
    @ApiResponse(responseCode = "200", description = "Test message emitted",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class)))
    public String testEndpoint() {
        log.info("testEndpoint");
        return "Still now working mate.";
    }
}

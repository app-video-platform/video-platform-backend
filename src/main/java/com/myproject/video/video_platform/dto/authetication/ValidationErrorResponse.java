package com.myproject.video.video_platform.dto.authetication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Schema(description = "Validation error payload listing field-level constraint violations.")
public class ValidationErrorResponse {
    @Schema(description = "Summary of why validation failed", example = "Validation failed")
    private String message;
    @Schema(description = "Map of field names to error messages", example = "{\"title\":\"Title is required\"}")
    private Map<String, String> errors;

    public ValidationErrorResponse(String message, Map<String, String> errors) {
        this.message = message;
        this.errors = errors;
    }
}

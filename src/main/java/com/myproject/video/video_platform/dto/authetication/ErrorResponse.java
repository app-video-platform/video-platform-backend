package com.myproject.video.video_platform.dto.authetication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Standard error payload returned for business or authentication failures.")
public class ErrorResponse {
    @Schema(description = "Human-readable explanation of the failure", example = "User account not verified")
    private String message;
    @Schema(description = "HTTP status code that triggered this body", example = "401")
    private int status;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }
}

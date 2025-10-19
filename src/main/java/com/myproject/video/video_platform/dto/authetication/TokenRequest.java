package com.myproject.video.video_platform.dto.authetication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Wrapper for verification or refresh tokens supplied via query string.")
public class TokenRequest {
    @NotBlank(message = "Token cannot be blank")
    @Schema(description = "Verification token value", example = "9d2e9b0f-6df4-4c3c-9b52-1b2c4c4b7c90")
    private String token;
}

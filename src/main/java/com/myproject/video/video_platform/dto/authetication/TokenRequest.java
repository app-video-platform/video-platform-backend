package com.myproject.video.video_platform.dto.authetication;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRequest {
    @NotBlank(message = "Token cannot be blank")
    private String token;
}

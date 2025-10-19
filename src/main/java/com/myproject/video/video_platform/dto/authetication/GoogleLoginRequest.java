package com.myproject.video.video_platform.dto.authetication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Google ID token payload obtained from OAuth client flow.")
public class GoogleLoginRequest {
    @Schema(description = "Opaque Google ID token", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2OTAifQ...")
    private String idToken;
}

package com.myproject.video.video_platform.exception.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthErrorResponse {
    private String error;             // e.g. "invalid_grant"
    private String error_description; // human-readable, safe to log briefly
}

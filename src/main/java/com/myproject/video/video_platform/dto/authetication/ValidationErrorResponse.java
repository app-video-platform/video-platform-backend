package com.myproject.video.video_platform.dto.authetication;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ValidationErrorResponse {
    private String message;
    private Map<String, String> errors;

    public ValidationErrorResponse(String message, Map<String, String> errors) {
        this.message = message;
        this.errors = errors;
    }
}


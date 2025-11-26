package com.myproject.video.video_platform.exception.product;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class QuizValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public QuizValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors == null ? Collections.emptyMap() : errors;
    }

}

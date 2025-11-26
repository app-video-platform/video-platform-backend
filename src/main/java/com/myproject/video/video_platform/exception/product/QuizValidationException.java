package com.myproject.video.video_platform.exception.product;

import java.util.Collections;
import java.util.Map;

public class QuizValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public QuizValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors == null ? Collections.emptyMap() : errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}

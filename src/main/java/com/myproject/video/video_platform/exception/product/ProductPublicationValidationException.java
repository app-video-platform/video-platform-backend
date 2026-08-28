package com.myproject.video.video_platform.exception.product;

import lombok.Getter;

import java.util.Map;

@Getter
public class ProductPublicationValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public ProductPublicationValidationException(Map<String, String> errors) {
        super("Product is not ready to publish");
        this.errors = Map.copyOf(errors);
    }
}

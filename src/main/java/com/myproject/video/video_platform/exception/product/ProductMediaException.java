package com.myproject.video.video_platform.exception.product;

public class ProductMediaException extends RuntimeException {
    public ProductMediaException(String message) { super(message); }
    public ProductMediaException(String message, Throwable cause) { super(message, cause); }
}

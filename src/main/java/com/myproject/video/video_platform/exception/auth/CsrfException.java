package com.myproject.video.video_platform.exception.auth;

public class CsrfException extends RuntimeException {
    public CsrfException(String message) {
        super(message);
    }
}

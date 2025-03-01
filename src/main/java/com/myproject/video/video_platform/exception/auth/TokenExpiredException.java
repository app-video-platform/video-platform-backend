package com.myproject.video.video_platform.exception.auth;

public class TokenExpiredException extends Exception {
    public TokenExpiredException(String message) {
        super(message);
    }
}

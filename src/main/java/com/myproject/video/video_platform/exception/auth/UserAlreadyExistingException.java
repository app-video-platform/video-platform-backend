package com.myproject.video.video_platform.exception.auth;

public class UserAlreadyExistingException extends RuntimeException {
    public UserAlreadyExistingException(String message) {
        super(message);
    }
}

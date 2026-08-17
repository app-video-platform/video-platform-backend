package com.myproject.video.video_platform.exception.commerce;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CommerceException extends RuntimeException {

    private final HttpStatus status;

    public CommerceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}

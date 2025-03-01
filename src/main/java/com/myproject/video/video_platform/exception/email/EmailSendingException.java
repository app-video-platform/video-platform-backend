package com.myproject.video.video_platform.exception.email;

public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}

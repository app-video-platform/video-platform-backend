package com.myproject.video.video_platform.exception.s3;

public class S3UploadException extends RuntimeException {
    public S3UploadException(String message) {
        super(message);
    }
}

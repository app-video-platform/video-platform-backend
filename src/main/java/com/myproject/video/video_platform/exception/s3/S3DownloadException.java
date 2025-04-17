package com.myproject.video.video_platform.exception.s3;

public class S3DownloadException extends RuntimeException {
  public S3DownloadException(String message) {
    super(message);
  }
}

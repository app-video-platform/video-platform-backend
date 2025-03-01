package com.myproject.video.video_platform.exception.auth;

public class AuthenticationException extends RuntimeException {
  public AuthenticationException(String message) {
    super(message);
  }
}

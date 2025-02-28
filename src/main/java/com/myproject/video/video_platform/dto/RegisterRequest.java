package com.myproject.video.video_platform.dto;

import lombok.Data;

/**
 * DTO for registration request payload.
 */
@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}

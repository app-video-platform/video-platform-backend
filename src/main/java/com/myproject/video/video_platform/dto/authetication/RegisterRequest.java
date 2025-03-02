package com.myproject.video.video_platform.dto.authetication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for registration request payload.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 8, max = 50, message = "First name must be between 1 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;
}

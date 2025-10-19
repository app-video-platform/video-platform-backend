package com.myproject.video.video_platform.dto.authetication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for registration request payload.
 */
@Schema(description = "Registration payload for creating a new teacher account.")
@Data
public class RegisterRequest {

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 8, max = 50, message = "First name must be between 1 and 50 characters")
    @Schema(description = "Teacher given name", example = "Amelia")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Schema(description = "Teacher family name", example = "Hughes")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Schema(description = "Login email", example = "amelia.hughes@example.com")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Schema(description = "Plain text password that will be encrypted on save", example = "Te@cherPass1")
    private String password;
}

package com.myproject.video.video_platform.dto.authetication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Credentials required to exchange for JWT and refresh cookies.")
public class LoginRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Schema(description = "Registered account email", example = "amelia.hughes@example.com")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    @Schema(description = "Account password", example = "Te@cherPass1")
    private String password;
}

package com.myproject.video.video_platform.controller.auth;

import com.myproject.video.video_platform.dto.authetication.GoogleLoginRequest;
import com.myproject.video.video_platform.dto.authetication.LoginRequest;
import com.myproject.video.video_platform.dto.authetication.RegisterRequest;
import com.myproject.video.video_platform.dto.authetication.TokenRequest;
import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.exception.auth.TokenExpiredException;
import com.myproject.video.video_platform.service.security.AuthService;
import com.myproject.video.video_platform.service.security.GoogleSignInService;
import com.myproject.video.video_platform.service.security.VerificationTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling user registration, login, and email verification.
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Auth", description = "User registration, authentication, and token lifecycle endpoints.")
public class AuthController {

    private final AuthService authService;
    private final VerificationTokenService verificationTokenService;
    private final GoogleSignInService googleSignInService;

    public AuthController(AuthService authService,
                          VerificationTokenService verificationTokenService,
                          GoogleSignInService googleSignInService) {
        this.authService = authService;
        this.verificationTokenService = verificationTokenService;
        this.googleSignInService = googleSignInService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new teacher", description = "Creates a disabled teacher account and issues a verification email. Duplicate emails are rejected.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration accepted; verify email to activate",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"User registered successfully.\""))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Email already in use",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> register(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Registration details for the new teacher",
            required = true,
            content = @Content(schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(name = "teacher",
                            value = "{\n  \"firstName\": \"Amelia\",\n  \"lastName\": \"Hughes\",\n  \"email\": \"amelia.hughes@example.com\",\n  \"password\": \"Te@cherPass1\"\n}"))) @Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully.");
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify email token", description = "Activates a pending account when the verification token is valid and not expired.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account verified",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Account verified successfully!\""))),
            @ApiResponse(responseCode = "400", description = "Token invalid",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token expired",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> verifyAccount(@RequestParam("token") TokenRequest token) throws TokenExpiredException {
        verificationTokenService.verifyToken(token);
        return ResponseEntity.ok("Account verified successfully!");
    }

    @PostMapping("/login")
    @Operation(summary = "Login with credentials", description = "Validates credentials, issues JWT/refresh cookies, and returns a confirmation message. Requires the account to be verified.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Login successful!\""))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or unverified",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> login(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credentials used to sign in",
            required = true,
            content = @Content(schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(name = "credentials",
                            value = "{\n  \"email\": \"amelia.hughes@example.com\",\n  \"password\": \"Te@cherPass1\"\n}"))) @Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        authService.login(loginRequest, response);
        return ResponseEntity.ok("Login successful!");
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Clears JWT, refresh, and CSRF cookies to end the current browser session.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout complete",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Logged out.\"")))
    })
    public ResponseEntity<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok("Logged out.");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh session tokens", description = "Rotates refresh and JWT cookies when a valid refresh token cookie is present.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Refresh successful\""))),
            @ApiResponse(responseCode = "401", description = "Refresh token missing or invalid",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authService.extractCookie(request, "REFRESH_TOKEN");
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Missing refresh token");
        }
        authService.refreshTokens(response, refreshToken);

        return ResponseEntity.ok("Refresh successful");
    }

    @PostMapping("/googleSignIn")
    @Operation(summary = "Login with Google", description = "Validates a Google ID token, creates the user if needed, and issues authentication cookies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sign-in successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Google sign-in successful\""))),
            @ApiResponse(responseCode = "401", description = "Google token invalid",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> googleLogin(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Google ID token obtained client-side",
            required = true,
            content = @Content(schema = @Schema(implementation = GoogleLoginRequest.class),
                    examples = @ExampleObject(name = "google", value = "{\n  \"idToken\": \"eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2OTAifQ...\"\n}"))) @RequestBody GoogleLoginRequest googleToken,
                                              HttpServletResponse response) {
        try {
            googleSignInService.handleSignIn(googleToken, response);
            return ResponseEntity.ok("Google sign-in successful");
        } catch (Exception ex) {
            log.error("Google login error: {}", ex.getMessage());
            return ResponseEntity.status(401).body("Google sign-in failed: " + ex.getMessage());
        }
    }
}

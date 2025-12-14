package com.myproject.video.video_platform.controller.docs.auth;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.GoogleLoginRequest;
import com.myproject.video.video_platform.dto.authetication.LoginRequest;
import com.myproject.video.video_platform.dto.authetication.RegisterRequest;
import com.myproject.video.video_platform.dto.authetication.TokenRequest;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.exception.auth.TokenExpiredException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public interface AuthApiDoc {

    @Operation(
            summary = "Register new teacher",
            description = "Creates a disabled teacher account and issues a verification email. Duplicate emails are rejected.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Registration details for the new teacher",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "teacher",
                                    value = "{\n  \"firstName\": \"Amelia\",\n  \"lastName\": \"Hughes\",\n  \"email\": \"amelia.hughes@example.com\",\n  \"password\": \"Te@cherPass1\"\n}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration accepted; verify email to activate",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"User registered successfully.\""))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Email already in use",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<String> register(@Valid RegisterRequest request);

    @Operation(
            summary = "Verify email token",
            description = "Activates a pending account when the verification token is valid and not expired."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account verified",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Account verified successfully!\""))),
            @ApiResponse(responseCode = "400", description = "Token invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token expired",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<String> verifyAccount(TokenRequest token) throws TokenExpiredException;

    @Operation(
            summary = "Login with credentials",
            description = "Validates credentials, issues JWT/refresh cookies, and returns a confirmation message. Requires the account to be verified.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credentials used to sign in",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "credentials",
                                    value = "{\n  \"email\": \"amelia.hughes@example.com\",\n  \"password\": \"Te@cherPass1\"\n}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Login successful!\""))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or unverified",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<String> login(@Valid LoginRequest loginRequest, @Parameter(hidden = true) HttpServletResponse response);

    @Operation(
            summary = "Logout user",
            description = "Clears JWT, refresh, and CSRF cookies to end the current browser session."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout complete",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Logged out.\"")))
    })
    ResponseEntity<String> logout(@Parameter(hidden = true) HttpServletResponse response);

    @Operation(
            summary = "Refresh session tokens",
            description = "Rotates refresh and JWT cookies when a valid refresh token cookie is present."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Refresh successful\""))),
            @ApiResponse(responseCode = "401", description = "Refresh token missing or invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<String> refresh(
            @Parameter(hidden = true) HttpServletRequest request,
            @Parameter(hidden = true) HttpServletResponse response
    );

    @Operation(
            summary = "Login with Google",
            description = "Validates a Google ID token, creates the user if needed, and issues authentication cookies.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Google ID token obtained client-side",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GoogleLoginRequest.class),
                            examples = @ExampleObject(
                                    name = "google",
                                    value = "{\n  \"idToken\": \"eyJhbGciOiJSUzI1NiIsImtpZCI6IjE2OTAifQ...\"\n}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sign-in successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Google sign-in successful\""))),
            @ApiResponse(responseCode = "401", description = "Google token invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<String> googleLogin(GoogleLoginRequest googleToken, @Parameter(hidden = true) HttpServletResponse response);
}

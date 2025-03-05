package com.myproject.video.video_platform.controller.auth;

import com.myproject.video.video_platform.dto.authetication.LoginRequest;
import com.myproject.video.video_platform.dto.authetication.RegisterRequest;
import com.myproject.video.video_platform.dto.authetication.TokenRequest;
import com.myproject.video.video_platform.exception.auth.TokenExpiredException;
import com.myproject.video.video_platform.service.security.AuthService;
import com.myproject.video.video_platform.service.security.VerificationTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
public class AuthController {

    private final AuthService authService;
    private final VerificationTokenService verificationTokenService;

    public AuthController(AuthService authService,
                          VerificationTokenService verificationTokenService) {
        this.authService = authService;
        this.verificationTokenService = verificationTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully.");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") TokenRequest token) throws TokenExpiredException {
        verificationTokenService.verifyToken(token);
        return ResponseEntity.ok("Account verified successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        authService.login(loginRequest, response);
        return ResponseEntity.ok("Login successful!");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.ok("Logged out.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authService.extractCookie(request, "REFRESH_TOKEN");
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("Missing refresh token");
        }
        authService.refreshTokens(response, refreshToken);

        return ResponseEntity.ok("Refresh successful");
    }
}

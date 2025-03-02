package com.myproject.video.video_platform.controller.auth;

import com.myproject.video.video_platform.dto.authetication.LoginRequest;
import com.myproject.video.video_platform.dto.authetication.LoginResponse;
import com.myproject.video.video_platform.dto.authetication.RegisterRequest;
import com.myproject.video.video_platform.dto.authetication.TokenRequest;
import com.myproject.video.video_platform.exception.auth.TokenExpiredException;
import com.myproject.video.video_platform.service.security.AuthService;
import com.myproject.video.video_platform.service.security.VerificationTokenService;
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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(loginResponse);
    }
}

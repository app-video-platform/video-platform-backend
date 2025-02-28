package com.myproject.video.video_platform.controller;

import com.myproject.video.video_platform.dto.RegisterRequest;
import com.myproject.video.video_platform.exception.TokenExpiredException;
import com.myproject.video.video_platform.service.security.AuthService;
import com.myproject.video.video_platform.service.security.VerificationTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final VerificationTokenService verificationTokenService;

    public AuthController(AuthService authService,
                          VerificationTokenService verificationTokenService) {
        this.authService = authService;
        this.verificationTokenService = verificationTokenService;
    }

    /**
     * Endpoint for user registration.
     * @param request registration details (email, password)
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            authService.register(request);
        }catch (Exception e){
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("User Registered Successfully! Check your email for verification link.");
    }

    /**
     * Endpoint for verifying email.
     * e.g. GET /api/auth/verify?token=some_token
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        try {
            verificationTokenService.verifyToken(token);
        } catch (TokenExpiredException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok("Account verified successfully!");
    }
}

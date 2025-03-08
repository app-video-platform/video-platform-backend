package com.myproject.video.video_platform.service.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CsrfProvider {

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a random token suitable for CSRF protection.
     */
    public String generateCsrfToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}

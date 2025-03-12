package com.myproject.video.video_platform.service.security;

import com.myproject.video.video_platform.entity.auth.RefreshToken;
import com.myproject.video.video_platform.exception.auth.AuthenticationException;
import com.myproject.video.video_platform.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.refresh-expiry-days}")
    private int refreshExpiryDays;

    private static final SecureRandom secureRandom = new SecureRandom();

    public String createRefreshToken(String userEmail) {
        byte[] tokenBytes = new byte[64];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUserEmail(userEmail);
        refreshToken.setExpiryDate(Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    public String validateRefreshToken(String token) {
        Optional<RefreshToken> opt = refreshTokenRepository.findByToken(token);
        if (opt.isEmpty()) {
            throw new AuthenticationException("Invalid refresh token");
        }
        RefreshToken rt = opt.get();

        if (rt.getExpiryDate().isBefore(Instant.now())) {
            // Remove expired token
            refreshTokenRepository.delete(rt);
            throw new AuthenticationException("Refresh token expired");
        }

        return rt.getUserEmail();
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}

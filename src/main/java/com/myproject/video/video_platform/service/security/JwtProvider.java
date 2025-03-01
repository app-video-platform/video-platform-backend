package com.myproject.video.video_platform.service.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Handles generating and validating JWT tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpirationInMillis;

    /**
     * Generate a JWT token for the given email (subject).
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .claim(Claims.SUBJECT, email)
                .claim(Claims.EXPIRATION, new Date(System.currentTimeMillis() + jwtExpirationInMillis))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validate the JWT token.
     */
    public boolean validateToken(String token) {
        try {
            // If parsing succeeds, the token is valid
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * Extract the email (subject) from the token.
     */
    public String getEmailFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Provide a custom JWT authentication filter to be added to the security chain.
     */
    public OncePerRequestFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(this);
    }

    /**
     * Convert the string secret into a SecretKey for signing.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}

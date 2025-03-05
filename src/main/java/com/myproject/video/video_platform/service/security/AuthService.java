package com.myproject.video.video_platform.service.security;

import com.myproject.video.video_platform.dto.authetication.LoginRequest;
import com.myproject.video.video_platform.dto.authetication.RegisterRequest;
import com.myproject.video.video_platform.entity.Role;
import com.myproject.video.video_platform.entity.User;
import com.myproject.video.video_platform.exception.auth.AuthenticationException;
import com.myproject.video.video_platform.repository.RoleRepository;
import com.myproject.video.video_platform.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;

/**
 * Handles user registration and login logic.
 */
@Slf4j
@Service
public class AuthService {

    @Value("${app.domain}")
    private String appDomain;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    private static final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       VerificationTokenService verificationTokenService,
                       JwtProvider jwtProvider,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenService = verificationTokenService;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
    }

    public void register(RegisterRequest request) {
        log.info("Register request: {}", request);
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);

        Role userRole = roleRepository.findByRoleName("user");
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);
        log.info("User registered and token will be sent : {}", user);

        verificationTokenService.createAndSendToken(user);
    }

    public void login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new AuthenticationException("Invalid credentials") {});

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new AuthenticationException("Invalid credentials");

        if (!user.isEnabled())
            throw new AuthenticationException("User account not verified");

        String jwtToken = jwtProvider.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        String csrfToken = generateCsrfToken();


        Cookie jwtCookie = new Cookie("JWT_TOKEN", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        jwtCookie.setDomain(appDomain);
        jwtCookie.setAttribute("SameSite", "None");

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        refreshCookie.setDomain(appDomain);
        refreshCookie.setAttribute("SameSite", "None");
        response.addCookie(refreshCookie);

        Cookie csrfCookie = new Cookie("XSRF-TOKEN", csrfToken);
        csrfCookie.setHttpOnly(false);
        csrfCookie.setSecure(true);
        csrfCookie.setPath("/");
        csrfCookie.setMaxAge(3600);

        response.addCookie(jwtCookie);
        response.addCookie(csrfCookie);
    }

    /**
     * Generates a random token suitable for CSRF protection.
     */
    public String generateCsrfToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public void logout(HttpServletResponse response) {
        // Clear cookies
        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        Cookie csrfCookie = new Cookie("XSRF-TOKEN", null);
        csrfCookie.setHttpOnly(false);
        csrfCookie.setSecure(true);
        csrfCookie.setPath("/");
        csrfCookie.setMaxAge(0);

        response.addCookie(jwtCookie);
        response.addCookie(csrfCookie);
    }

    /**
     * Called when user wants to refresh the JWT using the refresh token.
     */
    public void refreshTokens(HttpServletResponse response, String oldRefreshToken) {
        // 1) Validate refresh token from DB
        String userEmail = refreshTokenService.validateRefreshToken(oldRefreshToken);

        // 2) Generate new short-lived JWT
        String newAccessToken = jwtProvider.generateToken(userEmail);

        // 3) Rotate refresh token, remove old from DB, create a new one:
        refreshTokenService.deleteRefreshToken(oldRefreshToken);
        String newRefreshToken = refreshTokenService.createRefreshToken(userEmail);

        // 4) Set new cookies
        Cookie jwtCookie = new Cookie("JWT_TOKEN", newAccessToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        jwtCookie.setDomain(appDomain);
        jwtCookie.setAttribute("SameSite", "None");
        response.addCookie(jwtCookie);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        refreshCookie.setDomain(appDomain);
        refreshCookie.setAttribute("SameSite", "None");
        response.addCookie(refreshCookie);

        // Also update CSRF token if you want a new one
        Cookie csrfCookie = new Cookie("XSRF-TOKEN", generateCsrfToken());
        csrfCookie.setHttpOnly(false);
        csrfCookie.setSecure(true);
        csrfCookie.setPath("/");
        csrfCookie.setMaxAge(3600);
        response.addCookie(csrfCookie);
    }

    public String extractCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (cookieName.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}

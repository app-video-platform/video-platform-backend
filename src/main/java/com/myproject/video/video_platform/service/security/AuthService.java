package com.myproject.video.video_platform.service.security;

import com.myproject.video.video_platform.dto.authetication.LoginRequest;
import com.myproject.video.video_platform.dto.authetication.RegisterRequest;
import com.myproject.video.video_platform.entity.auth.Role;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.exception.auth.AuthenticationException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

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
    private final CsrfProvider csrfProvider;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       VerificationTokenService verificationTokenService,
                       JwtProvider jwtProvider,
                       RefreshTokenService refreshTokenService,
                       CsrfProvider csrfProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenService = verificationTokenService;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
        this.csrfProvider = csrfProvider;
    }

    public void register(RegisterRequest request) {
        log.info("Register request: {}", request);

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isPresent()) {
            throw new AuthenticationException("User already exists with same email: " + request.getEmail());
        } else {
            User user = new User();
            user.setUserId(UUID.randomUUID());
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
            //TODO: send welcome email
        }
    }

    public void login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new UserNotFoundException("No account with email: " + request.getEmail()) {});

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new AuthenticationException("Invalid credentials");

        if (!user.isEnabled())
            throw new AuthenticationException("User account not verified");

        setAuthCookies(response, user.getEmail());
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
    @Transactional
    public void refreshTokens(HttpServletResponse response, String oldRefreshToken) {
        String userEmail = refreshTokenService.validateRefreshToken(oldRefreshToken);
        refreshTokenService.deleteRefreshToken(oldRefreshToken);
        setAuthCookies(response, userEmail);
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

    public void setAuthCookies(HttpServletResponse response,
                                String userEmail) {

        String jwtToken = jwtProvider.generateToken(userEmail);
        String csrfToken = csrfProvider.generateCsrfToken();
        String refreshToken = refreshTokenService.createRefreshToken(userEmail);

        // JWT Cookie
        Cookie jwtCookie = new Cookie("JWT_TOKEN", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(3600);
        jwtCookie.setDomain(appDomain);
        jwtCookie.setAttribute("SameSite", "None");
        response.addCookie(jwtCookie);

        // Refresh Cookie
        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        refreshCookie.setDomain(appDomain);
        refreshCookie.setAttribute("SameSite", "None");
        response.addCookie(refreshCookie);

        // CSRF Cookie
        Cookie csrfCookie = new Cookie("XSRF-TOKEN", csrfToken);
        csrfCookie.setHttpOnly(false);
        csrfCookie.setSecure(true);
        csrfCookie.setAttribute("SameSite", "None");
        csrfCookie.setDomain(appDomain);
        csrfCookie.setPath("/");
        csrfCookie.setMaxAge(3600);
        response.addCookie(csrfCookie);
    }
}

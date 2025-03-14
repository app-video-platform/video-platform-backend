package com.myproject.video.video_platform.service.security;

import com.myproject.video.video_platform.exception.auth.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1) Get JWT from the cookie
        String jwt = extractJwtFromCookie(request);

        if (jwt != null) {
            if (!jwtProvider.validateToken(jwt)) {
                // 2a) Log the invalid token usage
                String clientIp = request.getRemoteAddr();
                String message = String.format("Invalid or expired token from IP=%s, token=%s", clientIp, jwt);
                log.warn(message);

                // 2b) Throw custom exception
                throw new InvalidTokenException("Invalid or expired token.");
            }

            // 3) If valid, set authentication
            String email = jwtProvider.getEmailFromJwt(jwt);
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    email,
                    null,
                    new ArrayList<>()
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("JWT_TOKEN".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}

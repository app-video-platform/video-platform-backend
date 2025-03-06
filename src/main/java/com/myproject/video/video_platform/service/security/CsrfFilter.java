package com.myproject.video.video_platform.service.security;


import com.myproject.video.video_platform.exception.auth.CsrfException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CsrfFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();
        // We only check CSRF for state-changing methods
        // AND we skip /api/auth/login (and optionally /api/auth/logout).
        if ((method.equalsIgnoreCase("POST")
                || method.equalsIgnoreCase("PUT")
                || method.equalsIgnoreCase("DELETE"
        ))
                && !path.equals("/api/auth/login")
                && !path.equals("/api/auth/logout")
                && !path.equals("/api/auth/refresh")) {

            String csrfCookie = extractCookie(request);
            String csrfHeader = request.getHeader("X-XSRF-TOKEN");

            if (csrfCookie == null || !csrfCookie.equals(csrfHeader)) {
                throw new CsrfException("CSRF token mismatch or missing.");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("XSRF-TOKEN".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}

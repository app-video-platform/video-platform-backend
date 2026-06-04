package com.myproject.video.video_platform.service.security;

import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void jwtCookieSetsUuidPrincipalAndRoleAuthorities() throws Exception {
        JwtProvider jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret", "12345678901234567890123456789012");
        ReflectionTestUtils.setField(jwtProvider, "jwtExpirationInMillis", 3600000L);

        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setUserId(userId);
        user.setEmail("creator@example.com");
        user.setRoles(Set.of(new Role(1L, UserRole.CREATOR.name())));

        String token = jwtProvider.generateToken(user);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/userInfo");
        request.setCookies(new Cookie("JWT_TOKEN", token));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Authentication> authentication = new AtomicReference<>();

        FilterChain chain = (servletRequest, servletResponse) ->
                authentication.set(SecurityContextHolder.getContext().getAuthentication());

        jwtProvider.jwtAuthenticationFilter().doFilter(request, response, chain);

        Authentication auth = authentication.get();
        assertEquals(userId.toString(), auth.getName());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CREATOR")));
    }
}

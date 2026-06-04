package com.myproject.video.video_platform.service.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.authetication.GoogleLoginRequest;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleSignInServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final AuthService authService = mock(AuthService.class);
    private final GoogleTokenVerifier googleTokenVerifier = mock(GoogleTokenVerifier.class);

    private final GoogleSignInService googleSignInService = new GoogleSignInService(
            userRepository,
            roleRepository,
            passwordEncoder,
            authService,
            googleTokenVerifier
    );

    @Test
    void handleSignIn_firstGoogleLoginCreatesEnabledUserWithDefaultUserRole() throws Exception {
        GoogleLoginRequest request = googleLoginRequest();
        GoogleIdToken.Payload payload = googlePayload();
        Role userRole = new Role(1L, UserRole.USER.name());
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(googleTokenVerifier.verify(request)).thenReturn(payload);
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("GOOGLE_LOGIN_google@example.com")).thenReturn("encoded-google-password");
        when(roleRepository.findByRoleName(UserRole.USER.name())).thenReturn(userRole);

        googleSignInService.handleSignIn(request, response);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("google@example.com", saved.getEmail());
        assertEquals("Grace", saved.getFirstName());
        assertEquals("Hopper", saved.getLastName());
        assertEquals("encoded-google-password", saved.getPassword());
        assertEquals("GOOGLE", saved.getAuthProvider());
        assertTrue(saved.isEnabled());
        assertTrue(saved.getRoles().contains(userRole));
        verify(authService).setAuthCookies(response, "google@example.com");
    }

    @Test
    void handleSignIn_firstGoogleLoginFailsIfDefaultUserRoleIsMissing() throws Exception {
        GoogleLoginRequest request = googleLoginRequest();

        when(googleTokenVerifier.verify(request)).thenReturn(googlePayload());
        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(UserRole.USER.name())).thenReturn(null);

        assertThrows(IllegalStateException.class, () ->
                googleSignInService.handleSignIn(request, new MockHttpServletResponse()));

        verify(userRepository, never()).save(org.mockito.Mockito.any());
        verify(authService, never()).setAuthCookies(org.mockito.Mockito.any(), org.mockito.Mockito.anyString());
    }

    private GoogleLoginRequest googleLoginRequest() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("google-id-token");
        return request;
    }

    private GoogleIdToken.Payload googlePayload() {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("google@example.com");
        payload.setEmailVerified(true);
        payload.set("given_name", "Grace");
        payload.set("family_name", "Hopper");
        payload.set("name", "Grace Hopper");
        return payload;
    }
}

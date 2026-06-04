package com.myproject.video.video_platform.service.security;

import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.authetication.RegisterRequest;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RoleRepository roleRepository = mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final VerificationTokenService verificationTokenService = mock(VerificationTokenService.class);
    private final JwtProvider jwtProvider = mock(JwtProvider.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final CsrfProvider csrfProvider = mock(CsrfProvider.class);

    private final AuthService authService = new AuthService(
            userRepository,
            roleRepository,
            passwordEncoder,
            verificationTokenService,
            jwtProvider,
            refreshTokenService,
            csrfProvider
    );

    @Test
    void register_assignsCanonicalUserRoleByDefault() {
        RegisterRequest request = registerRequest();
        Role userRole = new Role(1L, UserRole.USER.name());

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(roleRepository.findByRoleName(UserRole.USER.name())).thenReturn(userRole);

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("Amelia", saved.getFirstName());
        assertEquals("Hughes", saved.getLastName());
        assertEquals("amelia@example.com", saved.getEmail());
        assertEquals("encoded-password", saved.getPassword());
        assertTrue(saved.getRoles().contains(userRole));
        verify(verificationTokenService).createAndSendToken(saved);
    }

    @Test
    void register_failsIfDefaultUserRoleIsMissing() {
        RegisterRequest request = registerRequest();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(roleRepository.findByRoleName(UserRole.USER.name())).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
        verify(verificationTokenService, never()).createAndSendToken(any());
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Amelia");
        request.setLastName("Hughes");
        request.setEmail("amelia@example.com");
        request.setPassword("password123");
        return request;
    }
}

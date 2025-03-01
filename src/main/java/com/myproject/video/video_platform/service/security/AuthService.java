package com.myproject.video.video_platform.service.security;

import com.myproject.video.video_platform.dto.authetication.LoginResponse;
import com.myproject.video.video_platform.dto.authetication.RegisterRequest;
import com.myproject.video.video_platform.entity.Role;
import com.myproject.video.video_platform.entity.User;
import com.myproject.video.video_platform.exception.auth.AuthenticationException;
import com.myproject.video.video_platform.repository.RoleRepository;
import com.myproject.video.video_platform.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Handles user registration and login logic.
 */
@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       VerificationTokenService verificationTokenService,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenService = verificationTokenService;
        this.jwtProvider = jwtProvider;
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

    public LoginResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AuthenticationException("Invalid credentials") {});

        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
            throw new AuthenticationException("Invalid credentials");

        if (!user.isEnabled())
            throw new AuthenticationException("User account not verified");

        List<String> roles = user.getRoles().stream().map(Role::getRoleName).toList();

        return LoginResponse
                .builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(roles)
                .token(jwtProvider.generateToken(email))
                .build();
    }
}

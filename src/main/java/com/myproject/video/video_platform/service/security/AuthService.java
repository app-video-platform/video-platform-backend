package com.myproject.video.video_platform.service.security;

import com.myproject.video.video_platform.dto.RegisterRequest;
import com.myproject.video.video_platform.entity.Role;
import com.myproject.video.video_platform.entity.User;
import com.myproject.video.video_platform.repository.RoleRepository;
import com.myproject.video.video_platform.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

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

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       VerificationTokenService verificationTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenService = verificationTokenService;
    }

    public String register(RegisterRequest request) {
        log.info("Register request: {}", request);
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);

        Role userRole = roleRepository.findByRoleName("ROLE_USER");
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);
        log.info("User registered and token will be sent : {}", user);

        return verificationTokenService.createAndSendToken(user);
    }
}

package com.myproject.video.video_platform.service.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.myproject.video.video_platform.dto.authetication.GoogleLoginRequest;
import com.myproject.video.video_platform.entity.auth.Role;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.exception.auth.AuthenticationException;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class GoogleSignInService {

    @Value("${google.client-id}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public GoogleSignInService(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder,
                               AuthService authService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }


    public void handleSignIn(GoogleLoginRequest googleToken, HttpServletResponse response) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(googleClientId)).build();

            GoogleIdToken idToken = verifier.verify(googleToken.getIdToken());
            if (idToken == null) {
                log.error("Invalid Google ID token");
                throw new AuthenticationException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());
            String name = (String) payload.get("name");

            if (!emailVerified) {
                log.error("Google email not verified: {}", email);
                throw new AuthenticationException("Google email not verified");
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            User user;
            if (userOpt.isPresent()) {
                user = userOpt.get();
            } else {
                user = new User();
                user.setUserId(UUID.randomUUID());
                user.setEmail(email);
                user.setFirstName(name);
                // store random password
                user.setPassword(passwordEncoder.encode("GOOGLE_LOGIN_" + email));
                user.setEnabled(true);
                user.setAuthProvider("GOOGLE");

                Role userRole = roleRepository.findByRoleName("user");
                if (userRole == null) {
                    log.warn("Default role 'user' not found, creating or handle error");
                }
                user.setRoles(new HashSet<>(Collections.singleton(userRole)));
                userRepository.save(user);

                log.info("Created new user from Google sign-in: email={}, name={}", email, name);
                //TODO: send welcome email
            }
            authService.setAuthCookies(response, user.getEmail());

            log.info("Google sign-in success for email={}", email);
        } catch (Exception e) {
            log.error("Error verifying Google ID token: {}", e.getMessage());
            throw new AuthenticationException("Google sign-in failed");
        }
    }
}

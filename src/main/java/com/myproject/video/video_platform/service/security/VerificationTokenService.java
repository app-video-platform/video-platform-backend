package com.myproject.video.video_platform.service.security;

import com.myproject.video.video_platform.entity.User;
import com.myproject.video.video_platform.entity.VerificationToken;
import com.myproject.video.video_platform.exception.TokenExpiredException;
import com.myproject.video.video_platform.repository.UserRepository;
import com.myproject.video.video_platform.repository.VerificationTokenRepository;
import com.myproject.video.video_platform.service.mail.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for creating and verifying email tokens.
 */
@Slf4j
@Service
public class VerificationTokenService {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public VerificationTokenService(VerificationTokenRepository tokenRepository,
                                    UserRepository userRepository,
                                    EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void createAndSendToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(
                token,
                user,
                Instant.now().plusSeconds(86400) // 1 day expiry
        );
        log.info("Create token for user: {}", user);
        tokenRepository.save(verificationToken);

        String link = frontendUrl + "/verify-email?token=" + token;
        emailService.sendVerificationEmail(user.getEmail(), link);
    }

    public void verifyToken(String token) throws TokenExpiredException {
        Optional<VerificationToken> optionalToken = tokenRepository.findByToken(token);
        VerificationToken verificationToken = optionalToken.orElseThrow(
                () -> new RuntimeException("Invalid token!")
        );

        // Check expiry
        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenExpiredException("Token expired!");
        }

        // Enable the user
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        // Token used -> remove it or keep it for records
        tokenRepository.delete(verificationToken);
    }
}

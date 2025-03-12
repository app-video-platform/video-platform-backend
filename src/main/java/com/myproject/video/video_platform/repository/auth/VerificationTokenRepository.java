package com.myproject.video.video_platform.repository.auth;

import com.myproject.video.video_platform.entity.auth.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for VerificationToken entity.
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
}

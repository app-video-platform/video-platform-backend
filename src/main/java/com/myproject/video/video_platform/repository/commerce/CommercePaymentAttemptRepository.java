package com.myproject.video.video_platform.repository.commerce;

import com.myproject.video.video_platform.entity.commerce.CommercePaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommercePaymentAttemptRepository extends JpaRepository<CommercePaymentAttempt, UUID> {

    Optional<CommercePaymentAttempt> findByOrderId(UUID orderId);
}

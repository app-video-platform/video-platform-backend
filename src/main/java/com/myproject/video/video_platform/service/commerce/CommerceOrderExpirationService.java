package com.myproject.video.video_platform.service.commerce;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.common.enums.commerce.PaymentAttemptStatus;
import com.myproject.video.video_platform.repository.commerce.CommerceOrderRepository;
import com.myproject.video.video_platform.repository.commerce.CommercePaymentAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.commerce.enabled", havingValue = "true")
public class CommerceOrderExpirationService {

    private final CommerceOrderRepository orderRepository;
    private final CommercePaymentAttemptRepository paymentAttemptRepository;

    @Scheduled(fixedDelayString = "${app.commerce.expiration-scan-ms:60000}")
    @Transactional
    public void expirePendingOrders() {
        orderRepository.findAllByStatusAndExpiresAtBefore(
                CommerceOrderStatus.PENDING,
                Instant.now()
        ).forEach(order -> {
            order.setStatus(CommerceOrderStatus.EXPIRED);
            paymentAttemptRepository.findByOrderId(order.getId()).ifPresent(attempt -> {
                attempt.setStatus(PaymentAttemptStatus.EXPIRED);
                paymentAttemptRepository.save(attempt);
            });
            orderRepository.save(order);
        });
    }
}

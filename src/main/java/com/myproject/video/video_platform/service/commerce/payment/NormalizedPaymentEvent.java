package com.myproject.video.video_platform.service.commerce.payment;

import com.myproject.video.video_platform.common.enums.commerce.PaymentEventType;
import com.myproject.video.video_platform.common.enums.commerce.PaymentProvider;

import java.util.UUID;

public record NormalizedPaymentEvent(
        PaymentProvider provider,
        String providerEventId,
        UUID orderId,
        PaymentEventType eventType,
        String providerPaymentId,
        long amountMinor,
        String currency
) {
}

package com.myproject.video.video_platform.service.commerce.payment;

import java.time.Instant;
import java.util.UUID;

public record CheckoutGatewayCommand(
        UUID orderId,
        String buyerEmail,
        long amountMinor,
        String currency,
        Instant expiresAt
) {
}

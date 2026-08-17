package com.myproject.video.video_platform.dto.commerce;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.common.enums.commerce.PaymentProvider;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommerceOrderResponse(
        UUID orderId,
        CommerceOrderStatus status,
        PaymentProvider provider,
        String checkoutUrl,
        String currency,
        long subtotalMinor,
        long totalMinor,
        Instant expiresAt,
        Instant createdAt,
        Instant paidAt,
        Instant refundedAt,
        List<CommerceOrderItemResponse> items
) {
}

package com.myproject.video.video_platform.service.commerce.payment;

public record CheckoutGatewaySession(
        String providerSessionId,
        String checkoutUrl
) {
}

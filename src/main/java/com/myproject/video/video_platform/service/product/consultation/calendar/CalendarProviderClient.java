package com.myproject.video.video_platform.service.product.consultation.calendar;

import java.time.Instant;

public interface CalendarProviderClient {
    String buildAuthorizationUrl(String state, String loginHint);
    TokenBundle exchangeCodeForTokens(String code);
    String getAccountEmail(String accessToken);

    record TokenBundle(String accessToken, String refreshToken, Instant expiresAt) {}
}

package com.myproject.video.video_platform.service.product.consultation.calendar;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ICloudCalendarClient implements CalendarProviderClient {

    @Override
    public String buildAuthorizationUrl(String state, String loginHint) {
        // iCloud uses app-specific password; present a UI form instead of redirect.
        return null;
    }

    @Override
    public TokenBundle exchangeCodeForTokens(String code) {
        // Not applicable for iCloud; handled via credentials form later.
        return new TokenBundle(null, null, (Instant) null);
    }

    @Override
    public String getAccountEmail(String accessToken) {
        return null;
    }
}

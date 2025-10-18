package com.myproject.video.video_platform.service.security.calendar;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app.calendar")
public class CalendarProperties {

    private String stateSecret; // HMAC/AES key for state token signing
    private String cryptoSecret; // AES-GCM key for token encryption (base64-encoded 256-bit)

    @Data
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;  // e.g. https://api.example.com/api/calendars/oauth/google/callback
    }

    @Data
    public static class Microsoft {
        private String clientId;
        private String clientSecret;
        private String redirectUri; // e.g. https://api.example.com/api/calendars/oauth/microsoft/callback
        private String tenant = "common";
    }

    private Google google = new Google();
    private Microsoft microsoft = new Microsoft();
}

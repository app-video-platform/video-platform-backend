package com.myproject.video.video_platform.dto.products.consultation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

public class CalendarDtos {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProviderListResponse {
        private String[] providers; // ["GOOGLE","MICROSOFT","ICLOUD"]
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConnectInitRequest {
        @NotBlank
        private String provider; // GOOGLE|MICROSOFT|ICLOUD
        private String loginHint;          // optional email hint
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConnectInitResponse {
        private String authorizationUrl;   // FE should redirect here
        private String state;              // returned for debugging/tests (not necessary to FE)
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OAuthCallbackRequest {
        @NotBlank
        private String code;
        @NotBlank
        private String state;
        @NotBlank
        private String provider;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConnectedCalendarResponse {
        @NotNull
        private UUID id;
        @NotBlank
        private String provider;
        private String accountEmail;
        private Instant expiresAt;
    }
}

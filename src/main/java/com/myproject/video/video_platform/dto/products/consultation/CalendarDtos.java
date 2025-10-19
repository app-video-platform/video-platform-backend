package com.myproject.video.video_platform.dto.products.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Supported calendar providers for consultation sync.")
    public static class ProviderListResponse {
        @Schema(description = "Available provider codes", example = "[\"GOOGLE\",\"MICROSOFT\",\"ICLOUD\"]")
        private String[] providers; // ["GOOGLE","MICROSOFT","ICLOUD"]
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Request to start OAuth connect flow for a calendar provider.")
    public static class ConnectInitRequest {
        @NotBlank
        @Schema(description = "Selected provider", example = "GOOGLE")
        private String provider; // GOOGLE|MICROSOFT|ICLOUD
        @Schema(description = "Optional email hint shown on provider consent screen", example = "amelia.hughes@example.com")
        private String loginHint;          // optional email hint
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Response containing redirect URL and signed state for calendar connect.")
    public static class ConnectInitResponse {
        @Schema(description = "Authorization URL to redirect the teacher to", example = "https://accounts.google.com/o/oauth2/v2/auth?...state=...")
        private String authorizationUrl;   // FE should redirect here
        @Schema(description = "Opaque state parameter returned for test harnesses", example = "eyJ1aWQiOiI3MzgyOTdmMS0...")
        private String state;              // returned for debugging/tests (not necessary to FE)
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Payload received from provider after OAuth redirect.")
    public static class OAuthCallbackRequest {
        @NotBlank
        @Schema(description = "Provider authorization code", example = "4/0Adeu5B...")
        private String code;
        @NotBlank
        @Schema(description = "Signed state string returned during connect", example = "eyJ1aWQiOiI3MzgyOTdmMS0...")
        private String state;
        @NotBlank
        @Schema(description = "Provider identifier", example = "GOOGLE")
        private String provider;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(description = "Calendar connection summarised for UI display.")
    public static class ConnectedCalendarResponse {
        @NotNull
        @Schema(description = "Connection identifier", example = "e10f1e98-5a1d-4d1b-83ff-2be4a708f542")
        private UUID id;
        @NotBlank
        @Schema(description = "Provider identifier", example = "GOOGLE")
        private String provider;
        @Schema(description = "Primary account email when available", example = "amelia.hughes@example.com")
        private String accountEmail;
        @Schema(description = "Timestamp when the access token expires", example = "2024-05-01T10:00:00Z")
        private Instant expiresAt;
    }
}

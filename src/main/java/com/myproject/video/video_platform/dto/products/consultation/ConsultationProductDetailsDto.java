package com.myproject.video.video_platform.dto.products.consultation;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Locale;

@Data
@Schema(description = "Consultation-specific fields nested under product.details.")
public class ConsultationProductDetailsDto {

    @Schema(description = "Duration in minutes", example = "60")
    private Integer durationMinutes;

    @Schema(description = "Meeting delivery method", example = "ZOOM")
    private MeetingMethod meetingMethod;

    @Schema(description = "Optional custom meeting location", example = "https://cal.example.com/room/amelia")
    private String customLocation;

    @Schema(description = "Buffer before the session in minutes", example = "10")
    private Integer bufferBeforeMinutes;

    @Schema(description = "Buffer after the session in minutes", example = "10")
    private Integer bufferAfterMinutes;

    @Schema(description = "Maximum number of sessions per day", example = "5")
    private Integer maxSessionsPerDay;

    @Schema(description = "Message sent after booking")
    private String confirmationMessage;

    @Schema(description = "Cancellation policy presented to learners")
    private String cancellationPolicy;

    @Schema(description = "Calendars currently linked for real-time availability")
    private List<ConnectedCalendarDto> connectedCalendars;

    @Data
    @Schema(description = "Connected calendar integration summary.")
    public static class ConnectedCalendarDto {
        @Schema(description = "Calendar identifier", example = "e10f1e98-5a1d-4d1b-83ff-2be4a708f542")
        private String id;
        @Schema(description = "Calendar provider", example = "GOOGLE")
        private String provider;
        @Schema(description = "Token expiry timestamp", example = "2024-05-01T10:00:00Z")
        private String expiresAt;
    }

    public enum MeetingMethod {
        ZOOM,
        GOOGLE_MEET,
        PHONE,
        OTHER;

        @JsonCreator
        public static MeetingMethod fromJson(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }

            String normalized = value.trim()
                    .toUpperCase(Locale.ROOT)
                    .replace('-', '_')
                    .replace(' ', '_');

            return switch (normalized) {
                case "ZOOM", "ZOOM_MEETING" -> ZOOM;
                case "GOOGLE", "GOOGLE_MEET", "GOOGLE_MEETING" -> GOOGLE_MEET;
                case "PHONE", "PHONE_CALL", "CALL", "TELEPHONE" -> PHONE;
                case "OTHER", "CUSTOM" -> OTHER;
                default -> throw new IllegalArgumentException("Unknown meetingMethod: " + value);
            };
        }

    }
}

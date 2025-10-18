package com.myproject.video.video_platform.dto.products.consultation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("CONSULTATION")
public class ConsultationProductResponseDto extends AbstractProductResponseDto {

    private Integer durationMinutes;
    private MeetingMethod meetingMethod;
    private String customLocation;
    private Integer bufferBeforeMinutes;
    private Integer bufferAfterMinutes;
    private Integer maxSessionsPerDay;
    private String confirmationMessage;
    private String cancellationPolicy;

    /**
     * Summaries of calendars the teacher has connected.
     */
    private List<ConnectedCalendarDto> connectedCalendars;

    @Data
    public static class ConnectedCalendarDto {
        private String id;
        private String provider;
        private String expiresAt;
    }

    public enum MeetingMethod {
        ZOOM,
        GOOGLE_MEET,
        PHONE,
        OTHER
    }
}

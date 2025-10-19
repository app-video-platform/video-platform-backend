package com.myproject.video.video_platform.dto.products.consultation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("CONSULTATION")
@Schema(description = "Payload for defining one-to-one consultation slots.")
public class ConsultationProductRequestDto extends AbstractProductRequestDto {

    @NotNull
    @Min(15)
    @Max(240)
    @Schema(description = "Duration in minutes", example = "60", minimum = "15", maximum = "240")
    private Integer durationMinutes;

    @NotNull
    @Schema(description = "Meeting delivery method", example = "ZOOM")
    private MeetingMethod meetingMethod;

    @Schema(description = "Optional custom meeting location", example = "https://cal.serious-debauchery.click/room/amelia")
    private String customLocation;

    @NotNull
    @Min(0)
    @Schema(description = "Buffer before the session in minutes", example = "10", minimum = "0")
    private Integer bufferBeforeMinutes;

    @NotNull
    @Min(0)
    @Schema(description = "Buffer after the session in minutes", example = "10", minimum = "0")
    private Integer bufferAfterMinutes;

    @Min(1)
    @Schema(description = "Maximum number of sessions per calendar day", example = "5", minimum = "1")
    private Integer maxSessionsPerDay;

    @Schema(description = "Message sent after booking", example = "Thanks for booking! Please check your inbox for the Zoom link.")
    private String confirmationMessage;

    @NotBlank
    @Schema(description = "Cancellation policy shown during checkout", example = "Reschedule up to 24h before your slot; no-shows are non-refundable.")
    private String cancellationPolicy;

    public enum MeetingMethod {
        ZOOM,
        GOOGLE_MEET,
        PHONE,
        OTHER
    }
}

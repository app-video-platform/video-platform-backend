package com.myproject.video.video_platform.dto.products.consultation;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeName("CONSULTATION")
public class ConsultationProductRequestDto extends AbstractProductRequestDto {

    @NotNull
    @Min(15)
    @Max(240)
    private Integer durationMinutes;

    @NotNull
    private MeetingMethod meetingMethod;

    private String customLocation;

    @NotNull
    @Min(0)
    private Integer bufferBeforeMinutes;

    @NotNull
    @Min(0)
    private Integer bufferAfterMinutes;

    @Min(1)
    private Integer maxSessionsPerDay;

    private String confirmationMessage;

    @NotBlank
    private String cancellationPolicy;

    public enum MeetingMethod {
        ZOOM,
        GOOGLE_MEET,
        PHONE,
        OTHER
    }
}

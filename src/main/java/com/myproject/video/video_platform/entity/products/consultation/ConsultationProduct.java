package com.myproject.video.video_platform.entity.products.consultation;

import com.myproject.video.video_platform.entity.products.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "consultation_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationProduct extends Product {

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "meeting_method", length = 32)
    @Enumerated(EnumType.STRING)
    private MeetingMethod meetingMethod;

    @Column(name = "custom_location")
    private String customLocation;

    @Column(name = "buffer_before_minutes")
    private Integer bufferBeforeMinutes;

    @Column(name = "buffer_after_minutes")
    private Integer bufferAfterMinutes;

    @Column(name = "max_sessions_per_day")
    private Integer maxSessionsPerDay;

    @Column(name = "confirmation_message", columnDefinition = "TEXT")
    private String confirmationMessage;

    @Column(name = "cancellation_policy", columnDefinition = "TEXT")
    private String cancellationPolicy;


    public enum MeetingMethod {
        ZOOM,
        GOOGLE_MEET,
        PHONE,
        OTHER
    }
}

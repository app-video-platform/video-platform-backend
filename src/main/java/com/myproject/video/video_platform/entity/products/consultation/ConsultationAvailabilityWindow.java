package com.myproject.video.video_platform.entity.products.consultation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "consultation_availability_windows")
@Getter
@Setter
public class ConsultationAvailabilityWindow {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "availability_day_id", nullable = false)
    private ConsultationAvailabilityDay day;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private int position;
}

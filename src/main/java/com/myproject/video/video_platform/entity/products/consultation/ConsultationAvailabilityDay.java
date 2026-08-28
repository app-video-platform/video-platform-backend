package com.myproject.video.video_platform.entity.products.consultation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "consultation_availability_days", uniqueConstraints =
        @UniqueConstraint(name = "uq_consultation_availability_day", columnNames = {"consultation_product_id", "weekday"}))
@Getter
@Setter
public class ConsultationAvailabilityDay {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultation_product_id", nullable = false)
    private ConsultationProduct consultation;

    @Enumerated(EnumType.STRING)
    @Column(name = "weekday", nullable = false, length = 16)
    private Weekday weekday;

    @Column(nullable = false)
    private boolean enabled;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ConsultationAvailabilityWindow> windows = new ArrayList<>();

    public enum Weekday { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }
}

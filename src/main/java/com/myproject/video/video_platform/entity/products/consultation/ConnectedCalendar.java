package com.myproject.video.video_platform.entity.products.consultation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "consultation_connected_calendars")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectedCalendar {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "provider", length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "oauth_token_enc", columnDefinition = "TEXT", nullable = false)
    private String oauthTokenEnc;

    @Column(name = "refresh_token_enc", columnDefinition = "TEXT", nullable = false)
    private String refreshTokenEnc;

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    public enum Provider {
        GOOGLE,
        MICROSOFT,
        ICLOUD
    }
}

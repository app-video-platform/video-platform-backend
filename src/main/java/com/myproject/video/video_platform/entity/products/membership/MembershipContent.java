package com.myproject.video.video_platform.entity.products.membership;

import com.myproject.video.video_platform.common.enums.membership.MembershipContentStatus;
import com.myproject.video.video_platform.common.enums.membership.MembershipContentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "membership_content")
@Getter
@Setter
@NoArgsConstructor
public class MembershipContent {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_product_id", nullable = false)
    private MembershipProduct membership;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipContentType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipContentStatus status;

    @Column(columnDefinition = "TEXT")
    private String body;

    private UUID assetFileId;
    private String assetFileName;
    private String assetFileType;
    private Long assetSize;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

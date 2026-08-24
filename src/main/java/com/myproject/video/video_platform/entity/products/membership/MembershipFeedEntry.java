package com.myproject.video.video_platform.entity.products.membership;

import com.myproject.video.video_platform.common.enums.membership.MembershipFeedEntryKind;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "membership_feed_entries")
@Getter
@Setter
@NoArgsConstructor
public class MembershipFeedEntry {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "membership_product_id", nullable = false)
    private MembershipProduct membership;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipFeedEntryKind kind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private MembershipContent content;

    private UUID includedProductId;

    @Column(nullable = false)
    private Instant addedAt;

    private Integer position;
}

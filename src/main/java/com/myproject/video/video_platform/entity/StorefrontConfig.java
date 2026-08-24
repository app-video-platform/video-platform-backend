package com.myproject.video.video_platform.entity;

import com.myproject.video.video_platform.common.enums.StorefrontAppearance;
import com.myproject.video.video_platform.common.enums.StorefrontTypography;
import com.myproject.video.video_platform.entity.user.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "storefront_configs")
@Getter
@Setter
@NoArgsConstructor
public class StorefrontConfig {
    @Id @GeneratedValue private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false, unique = true)
    private User creator;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private StorefrontAppearance appearance = StorefrontAppearance.DARK;
    @Column(name = "accent_color", nullable = false, length = 7)
    private String accentColor = "#ffbd41";
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private StorefrontTypography typography = StorefrontTypography.MODERN;
    @Column(name = "featured_product_id") private UUID featuredProductId;

    @ElementCollection
    @CollectionTable(name = "storefront_product_order", joinColumns = @JoinColumn(name = "storefront_config_id"))
    @Column(name = "product_id", nullable = false)
    @OrderColumn(name = "position")
    private List<UUID> productOrderIds = new ArrayList<>();

    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

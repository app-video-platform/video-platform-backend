package com.myproject.video.video_platform.entity;

import com.myproject.video.video_platform.common.enums.ProductLandingHeroLayout;
import com.myproject.video.video_platform.common.enums.ProductLandingSection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "product_landing_page_configs")
@Getter
@Setter
@NoArgsConstructor
public class ProductLandingPageConfig {
    @Id @GeneratedValue private UUID id;
    @Column(name = "product_id", nullable = false, unique = true) private UUID productId;
    @Column(name = "marketing_description", length = 1200) private String marketingDescription;
    @Enumerated(EnumType.STRING) @Column(name = "hero_layout", nullable = false, length = 20)
    private ProductLandingHeroLayout heroLayout = ProductLandingHeroLayout.MEDIA_RIGHT;

    @ElementCollection
    @CollectionTable(name = "product_landing_visible_sections", joinColumns = @JoinColumn(name = "config_id"))
    @Enumerated(EnumType.STRING) @Column(name = "section_id", nullable = false, length = 16)
    @OrderColumn(name = "position")
    private List<ProductLandingSection> visibleSections = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_landing_section_order", joinColumns = @JoinColumn(name = "config_id"))
    @Enumerated(EnumType.STRING) @Column(name = "section_id", nullable = false, length = 16)
    @OrderColumn(name = "position")
    private List<ProductLandingSection> sectionOrder = new ArrayList<>();

    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

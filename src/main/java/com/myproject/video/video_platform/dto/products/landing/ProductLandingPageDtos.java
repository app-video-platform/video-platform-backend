package com.myproject.video.video_platform.dto.products.landing;

import com.myproject.video.video_platform.common.enums.ProductLandingHeroLayout;
import com.myproject.video.video_platform.common.enums.ProductLandingSection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class ProductLandingPageDtos {
    private ProductLandingPageDtos() {}

    public record Config(
            String id,
            String productId,
            String marketingDescription,
            ProductLandingHeroLayout heroLayout,
            List<ProductLandingSection> visibleSections,
            List<ProductLandingSection> sectionOrder,
            Instant updatedAt
    ) {}

    public record UpdateRequest(
            @Size(max = 1200) String marketingDescription,
            @NotNull ProductLandingHeroLayout heroLayout,
            @NotNull List<@NotNull ProductLandingSection> visibleSections,
            @NotNull List<@NotNull ProductLandingSection> sectionOrder
    ) {}
}

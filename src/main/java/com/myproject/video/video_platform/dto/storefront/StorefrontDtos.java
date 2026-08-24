package com.myproject.video.video_platform.dto.storefront;

import com.myproject.video.video_platform.common.enums.StorefrontAppearance;
import com.myproject.video.video_platform.common.enums.StorefrontTypography;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.user.SocialMediaLinkResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.List;

public final class StorefrontDtos {
    private StorefrontDtos() {}

    public record Theme(
            @NotNull StorefrontAppearance appearance,
            @NotNull @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String accentColor,
            @NotNull StorefrontTypography typography
    ) {}

    public record Config(
            String id,
            String featuredProductId,
            List<String> productOrderIds,
            Theme theme,
            Instant updatedAt
    ) {}

    public record UpdateRequest(
            String featuredProductId,
            @NotNull List<String> productOrderIds,
            @NotNull @Valid Theme theme
    ) {}

    public record PublicCreator(
            String id,
            String displayName,
            String email,
            String title,
            String tagline,
            String bio,
            String website,
            String publicEmail,
            String imageUrl,
            List<SocialMediaLinkResponse> socialLinks
    ) {}

    public record PublicProduct(
            String id,
            String title,
            String description,
            ProductType type,
            ProductStatus status,
            Object price,
            String imageUrl
    ) {}

    public record PublicStorefront(
            String id,
            PublicCreator creator,
            String featuredProductId,
            List<PublicProduct> products,
            Theme theme
    ) {}
}

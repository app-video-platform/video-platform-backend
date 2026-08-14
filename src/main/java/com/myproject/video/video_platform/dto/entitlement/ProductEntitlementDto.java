package com.myproject.video.video_platform.dto.entitlement;

import com.myproject.video.video_platform.common.enums.entitlement.EntitlementSource;
import com.myproject.video.video_platform.dto.products.ProductMinimised;

import java.time.Instant;
import java.util.UUID;

public record ProductEntitlementDto(
        UUID id,
        EntitlementSource source,
        Instant createdAt,
        ProductMinimised product
) {
}

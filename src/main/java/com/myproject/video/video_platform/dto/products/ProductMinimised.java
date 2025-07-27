package com.myproject.video.video_platform.dto.products;

import com.myproject.video.video_platform.common.enums.products.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductMinimised(
        UUID id,
        String title,
        ProductType type,
        BigDecimal price,
        UUID createdById,
        String createdByName,
        String createdByTitle,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

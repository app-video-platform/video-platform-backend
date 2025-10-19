package com.myproject.video.video_platform.dto.products;

import com.myproject.video.video_platform.common.enums.products.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Lightweight projection used in listings and search results.")
public record ProductMinimised(
        @Schema(description = "Product identifier", example = "a8c5d4a9-dc93-4c71-9c33-5d56f3d6b21d") UUID id,
        @Schema(description = "Product title", example = "Foundations of Lifestyle Photography") String title,
        @Schema(description = "Product type", example = "COURSE") ProductType type,
        @Schema(description = "Display price", example = "149.00") BigDecimal price,
        @Schema(description = "Creator identifier", example = "738297f1-45fb-4f5f-98a5-6d0eb0a8f542") UUID createdById,
        @Schema(description = "Creator full name", example = "Amelia Hughes") String createdByName,
        @Schema(description = "Creator job title/role", example = "Lifestyle Photographer") String createdByTitle,
        @Schema(description = "Creation timestamp", example = "2024-03-28T11:24:00") LocalDateTime createdAt,
        @Schema(description = "Last update timestamp", example = "2024-04-02T09:15:30") LocalDateTime updatedAt
) {}

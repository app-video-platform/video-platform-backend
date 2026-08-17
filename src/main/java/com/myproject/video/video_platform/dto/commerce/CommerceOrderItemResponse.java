package com.myproject.video.video_platform.dto.commerce;

import com.myproject.video.video_platform.common.enums.products.ProductType;

import java.util.UUID;

public record CommerceOrderItemResponse(
        UUID id,
        UUID productId,
        ProductType productType,
        String productName,
        long unitAmountMinor,
        int quantity,
        long lineTotalMinor
) {
}

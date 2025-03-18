package com.myproject.video.video_platform.service.product.strategy_handler;

import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductResponseDto;

public interface ProductTypeHandler {
    ProductType getSupportedType();
    AbstractProductResponseDto getProductById(String productId);
}

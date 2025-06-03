package com.myproject.video.video_platform.service.product.strategy_handler;

import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;

public interface ProductTypeHandler {

    ProductType getSupportedType();

    AbstractProductResponseDto getProductById(String productId);

    AbstractProductResponseDto createProduct(AbstractProductRequestDto dto);

    AbstractProductResponseDto updateProduct(AbstractProductRequestDto dto);

    void deleteProduct(String userId, String productId);
}

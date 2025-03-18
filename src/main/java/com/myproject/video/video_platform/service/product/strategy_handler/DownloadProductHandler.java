package com.myproject.video.video_platform.service.product.strategy_handler;

import com.myproject.video.video_platform.common.converter.product.DownloadProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductResponseDto;
import com.myproject.video.video_platform.entity.products.download_product.DownloadProduct;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.download_product.DownloadProductRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DownloadProductHandler implements ProductTypeHandler {

    private final DownloadProductRepository downloadProductRepository;
    private final DownloadProductConverter downloadProductConverter;

    public DownloadProductHandler(DownloadProductRepository downloadProductRepository,
                                  DownloadProductConverter downloadProductConverter) {
        this.downloadProductRepository = downloadProductRepository;
        this.downloadProductConverter = downloadProductConverter;
    }

    @Override
    public ProductType getSupportedType() {
        return ProductType.DOWNLOAD;
    }

    @Override
    public AbstractProductResponseDto getProductById(String productId) {
        UUID id = UUID.fromString(productId);
        DownloadProduct product = downloadProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DownloadProduct not found for ID: " + productId));
        return downloadProductConverter.mapDownloadProductToResponse(product);
    }
}

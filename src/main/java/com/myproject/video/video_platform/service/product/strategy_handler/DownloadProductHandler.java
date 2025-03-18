package com.myproject.video.video_platform.service.product.strategy_handler;

import com.myproject.video.video_platform.common.converter.product.DownloadProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products_creation.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products_creation.DownloadProductRequestDto;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.download_product.DownloadProduct;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.products.download_product.DownloadProductRepository;
import com.myproject.video.video_platform.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class DownloadProductHandler implements ProductTypeHandler {

    private final DownloadProductRepository downloadProductRepository;
    private final DownloadProductConverter downloadProductConverter;
    private final UserService userService;

    public DownloadProductHandler(DownloadProductRepository downloadProductRepository,
                                  DownloadProductConverter downloadProductConverter,
                                  UserService userService) {
        this.downloadProductRepository = downloadProductRepository;
        this.downloadProductConverter = downloadProductConverter;
        this.userService = userService;
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

    @Override
    public AbstractProductResponseDto createProduct(AbstractProductRequestDto dto) {
        log.info("Creating a DownloadProduct: {}", dto.getName());

        Optional<User> userOptional = userService.findByUserId(UUID.fromString(dto.getUserId()));
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found with id: " + dto.getUserId());
        }

        DownloadProduct product = downloadProductConverter
                .mapDownloadProductRequestDtoToEntity((DownloadProductRequestDto) dto, userOptional.get());
        DownloadProduct saved = downloadProductRepository.save(product);

        log.info("Created succesfully a DownloadProduct: {}", dto.getName());
        return downloadProductConverter.mapDownloadProductToResponse(saved);
    }

    @Override
    public AbstractProductResponseDto updateProduct(AbstractProductRequestDto dto) {
        log.info("Updating a download product: {}", dto.getName());
        Optional<User> userOptional = userService.findByUserId(UUID.fromString(dto.getUserId()));
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found with id: " + dto.getUserId());
        }

        Optional<DownloadProduct> downloadProductOptional =
                downloadProductRepository.findById(UUID.fromString(dto.getId()));
        if (downloadProductOptional.isPresent()) {

            DownloadProduct updatedProduct = downloadProductConverter.mapDownloadProductUpdate(
                    downloadProductOptional.get(),
                    (DownloadProductRequestDto) dto
            );
            log.info("Updated succesfully a DownloadProduct: {}", dto.getName());
            return downloadProductConverter.mapDownloadProductToResponse(updatedProduct);
        } else
            throw new ResourceNotFoundException("DownloadProduct not found for ID: " + dto.getId());
    }
}

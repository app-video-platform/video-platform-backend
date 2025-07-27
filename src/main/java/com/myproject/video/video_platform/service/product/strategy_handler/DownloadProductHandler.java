package com.myproject.video.video_platform.service.product.strategy_handler;

import com.myproject.video.video_platform.common.converter.product.DownloadProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductRequestDto;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.products.download.DownloadProductRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import com.myproject.video.video_platform.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DownloadProductHandler implements ProductTypeHandler {

    private final DownloadProductRepository downloadProductRepository;
    private final DownloadProductConverter downloadProductConverter;
    private final UserService userService;
    private final CurrentUserService currentUserService;


    @Override
    public ProductType getSupportedType() {
        return ProductType.DOWNLOAD;
    }

    @Override
    public AbstractProductResponseDto getProductById(String productId) {
        UUID id = UUID.fromString(productId);
        DownloadProduct product = downloadProductRepository.findFullById(id)
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

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!userOptional.get().getUserId().equals(currentUserId))
            throw new AccessDeniedException("You don’t own this product.");

        DownloadProduct product = downloadProductConverter
                .mapDownloadProductRequestDtoToEntity((DownloadProductRequestDto) dto, userOptional.get());
        DownloadProduct saved = downloadProductRepository.save(product);

        log.info("Created succesfully a DownloadProduct: {}", dto.getName());
        return downloadProductConverter.mapDownloadProductToResponse(saved);
    }

    @Override
    public AbstractProductResponseDto updateProduct(AbstractProductRequestDto dto) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        log.info("Updating a download product: {}", dto.getName());

        Optional<DownloadProduct> downloadProductOptional =
                downloadProductRepository.findById(UUID.fromString(dto.getId()));

        if (downloadProductOptional.isPresent()) {
            DownloadProduct downloadProduct = downloadProductOptional.get();

            if (!downloadProduct.getUser().getUserId().equals(currentUserId))
                throw new AccessDeniedException("You don’t own this product.");

            DownloadProduct updatedProduct = downloadProductConverter.mapDownloadProductUpdate(
                    downloadProduct,
                    (DownloadProductRequestDto) dto
            );

            updatedProduct = downloadProductRepository.save(updatedProduct);

            log.info("Updated succesfully a DownloadProduct: {}", dto.getName());
            return downloadProductConverter.mapDownloadProductToResponse(updatedProduct);
        } else
            throw new ResourceNotFoundException("DownloadProduct not found for ID: " + dto.getId());
    }

    @Override
    public void deleteProduct(String userId, String productId) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        Optional<DownloadProduct> downloadProductOptional = downloadProductRepository.findById(UUID.fromString(productId));
        if (downloadProductOptional.isPresent()) {
            if (!downloadProductOptional.get().getUser().getUserId().equals(currentUserId))
                throw new AccessDeniedException("You don’t own this product.");

            downloadProductRepository.delete(downloadProductOptional.get());
            log.info("Deleted succesfully a DownloadProduct: {}", productId);
        } else
            throw new ResourceNotFoundException("DownloadProduct not found for ID: " + productId);
    }
}

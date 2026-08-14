package com.myproject.video.video_platform.service.entitlement;

import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.products.download.FileDownloadProduct;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.product.UnsupportedProductOperationException;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.repository.products.download.FileDownloadProductRepository;
import com.myproject.video.video_platform.service.digitalocean.SpacesS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductFileAccessService {

    private static final Duration DOWNLOAD_URL_LIFETIME = Duration.ofMinutes(10);

    private final ProductRepository productRepository;
    private final FileDownloadProductRepository fileRepository;
    private final ProductContentAccessService contentAccessService;
    private final SpacesS3Service spacesS3Service;

    @Transactional
    public String createDownloadUrl(UUID productId, UUID fileId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (product.getType() != ProductType.DOWNLOAD) {
            throw new UnsupportedProductOperationException(
                    "Downloads are only supported for DOWNLOAD products"
            );
        }
        contentAccessService.requireContentAccess(product);

        FileDownloadProduct file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
        if (!file.getSection().getDownloadProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException(
                    "File not found for product " + productId + ": " + fileId
            );
        }

        file.setDownloadCount(file.getDownloadCount() + 1);
        fileRepository.save(file);
        return spacesS3Service.generatePresignedUrlForGet(
                file.getPath(),
                DOWNLOAD_URL_LIFETIME
        );
    }
}

package com.myproject.video.video_platform.service.product.strategy_handler;

import com.myproject.video.video_platform.common.converter.product.ConsultationProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductRequestDto;
import com.myproject.video.video_platform.entity.products.consultation.ConsultationProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.consultation.ConsultationProductRepository;
import com.myproject.video.video_platform.service.product.ProductAuthorizationService;
import com.myproject.video.video_platform.service.product.ProductPublicationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsultationProductHandler implements ProductTypeHandler {

    private final ConsultationProductRepository repo;
    private final ConsultationProductConverter converter;
    private final ProductAuthorizationService productAuthorizationService;
    private final ProductPublicationValidator publicationValidator;

    @Override
    public ProductType getSupportedType() {
        return ProductType.CONSULTATION;
    }

    @Override
    @Transactional
    public AbstractProductResponseDto createProduct(AbstractProductRequestDto dto) {
        User owner = productAuthorizationService.resolveOwnerForCreate(dto);

        ConsultationProduct entity = converter.fromDto((ConsultationProductRequestDto) dto, owner);
        publicationValidator.validate(entity);
        ConsultationProduct saved = repo.save(entity);

        return converter.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractProductResponseDto getProductById(String productId) {
        ConsultationProduct entity = repo.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found: " + productId));
        return converter.toDto(entity);
    }

    @Override
    @Transactional
    public AbstractProductResponseDto updateProduct(AbstractProductRequestDto dto) {
        String id = dto.getId();
        ConsultationProduct existing = repo.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found: " + id));

        productAuthorizationService.requireOwnerOrAdmin(existing);

        converter.updateEntityFromDto(
                (ConsultationProductRequestDto) dto,
                existing
        );
        publicationValidator.validate(existing);

        ConsultationProduct updated = repo.save(existing);
        return converter.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(String userId, String productId) {
        log.info("Deleting Consultation: {}", productId);

        Optional<ConsultationProduct> existing = repo.findById(UUID.fromString(productId));

        if (existing.isEmpty())
            throw new ResourceNotFoundException("DownloadProduct not found for ID: " + productId);
        else {
            productAuthorizationService.requireOwnerOrAdmin(existing.get());

            repo.delete(existing.get());
            log.info("Deleted succesfully a Consultation Product: {}", productId);
        }

    }
}

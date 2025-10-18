package com.myproject.video.video_platform.service.product.strategy_handler;

import com.myproject.video.video_platform.common.converter.product.ConsultationProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductRequestDto;
import com.myproject.video.video_platform.entity.products.consultation.ConsultationProduct;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.products.consultation.ConsultationProductRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import com.myproject.video.video_platform.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
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
    private final UserService userService;
    private final CurrentUserService currentUserService;

    @Override
    public ProductType getSupportedType() {
        return ProductType.CONSULTATION;
    }

    @Override
    @Transactional
    public AbstractProductResponseDto createProduct(AbstractProductRequestDto dto) {
        log.info("Creating a new Consultation: {}", dto.getName());

        User owner = userService
                .findByUserId(UUID.fromString(dto.getUserId()))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + dto.getUserId()));

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!owner.getUserId().equals(currentUserId))
            throw new AccessDeniedException("You don’t own this product.");

        ConsultationProduct entity = converter.fromDto((ConsultationProductRequestDto) dto, owner);
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
        log.info("Updating Consultation: {}", dto.getName());

        String id = dto.getId();
        ConsultationProduct existing = repo.findById(UUID.fromString(id))
                .filter(p -> p.getUser().getUserId().equals(UUID.fromString(dto.getUserId())))
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found or not owned: " + id));


        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!existing.getUser().getUserId().equals(currentUserId))
            throw new AccessDeniedException("You don’t own this product.");

        converter.updateEntityFromDto(
                (ConsultationProductRequestDto) dto,
                existing
        );

        ConsultationProduct updated = repo.save(existing);
        return converter.toDto(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(String userId, String productId) {
        log.info("Deleting Consultation: {}", productId);

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);


        Optional<ConsultationProduct> existing = repo.findById(UUID.fromString(productId));

        if (existing.isEmpty())
            throw new ResourceNotFoundException("DownloadProduct not found for ID: " + productId);
        else {
            if (!existing.get().getUser().getUserId().equals(currentUserId))
                throw new AccessDeniedException("You don’t own this product.");

            repo.delete(existing.get());
            log.info("Deleted succesfully a Consultation Product: {}", productId);
        }

    }
}

package com.myproject.video.video_platform.service.product.strategy_handler;

import com.myproject.video.video_platform.common.converter.product.CourseProductConverter;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.AbstractProductRequestDto;
import com.myproject.video.video_platform.dto.products.AbstractProductResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.service.product.ProductAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseProductHandler implements ProductTypeHandler {

    private final CourseProductRepository courseRepo;
    private final CourseProductConverter converter;
    private final ProductAuthorizationService productAuthorizationService;

    @Override
    public ProductType getSupportedType() {
        return ProductType.COURSE;
    }

    @Override
    @Transactional(readOnly = true)
    public AbstractProductResponseDto getProductById(String productId) {
        UUID id = UUID.fromString(productId);
        CourseProduct course = courseRepo.findFullById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + productId));
        return converter.mapCourseToResponse(course);
    }

    @Override
    @Transactional
    public AbstractProductResponseDto createProduct(AbstractProductRequestDto baseDto) {
        CourseProductRequestDto dto = (CourseProductRequestDto) baseDto;
        log.info("Creating a new Course: {}", dto.getName());

        User owner = productAuthorizationService.resolveOwnerForCreate(dto);

        CourseProduct courseEntity = converter.mapCourseCreateDtoToEntity(dto, owner);
        CourseProduct saved = courseRepo.save(courseEntity);
        return converter.mapCourseToResponse(saved);
    }

    @Override
    @Transactional
    public AbstractProductResponseDto updateProduct(AbstractProductRequestDto baseDto) {
        CourseProductRequestDto dto = (CourseProductRequestDto) baseDto;
        log.info("Updating Course: {}", dto.getName());

        UUID id = UUID.fromString(dto.getId());
        CourseProduct existing = courseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + dto.getId()));

        productAuthorizationService.requireOwnerOrAdmin(existing);

        converter.applyCourseUpdateDto(existing, dto);
        CourseProduct saved = courseRepo.save(existing);
        return converter.mapCourseToResponse(saved);
    }

    @Override
    public void deleteProduct(String userId, String productId) {
        Optional<CourseProduct> courseProductOptional = courseRepo.findById(UUID.fromString(productId));
        if (courseProductOptional.isPresent()) {
            productAuthorizationService.requireOwnerOrAdmin(courseProductOptional.get());

            courseRepo.delete(courseProductOptional.get());
            log.info("Deleted succesfully a DownloadProduct: {}", productId);
        } else
            throw new ResourceNotFoundException("DownloadProduct not found for ID: " + productId);
    }
}

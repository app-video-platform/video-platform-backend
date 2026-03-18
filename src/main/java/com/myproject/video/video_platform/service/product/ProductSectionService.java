package com.myproject.video.video_platform.service.product;

import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.authoring.ProductSectionCreateRequestDto;
import com.myproject.video.video_platform.dto.products.authoring.ProductSectionResponseDto;
import com.myproject.video.video_platform.dto.products.authoring.ProductSectionUpdateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.dto.products.download.FileDownloadProductResponseDto;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import com.myproject.video.video_platform.entity.products.download.DownloadProduct;
import com.myproject.video.video_platform.entity.products.download.FileDownloadProduct;
import com.myproject.video.video_platform.entity.products.download.SectionDownloadProduct;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.product.UnsupportedProductOperationException;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.repository.products.download.DownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download.SectionDownloadProductRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSectionService {

    @Value("${digitalocean.spaces.cdnEndpointUrl}")
    private String cdnEndpointUrl;

    @Value("${digitalocean.spaces.bucket-media}")
    private String mediaBucket;

    private final ProductRepository productRepository;
    private final CourseProductRepository courseProductRepository;
    private final DownloadProductRepository downloadProductRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final SectionDownloadProductRepository downloadSectionRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public ProductSectionResponseDto createSection(String productId, ProductSectionCreateRequestDto dto) {
        Product product = getOwnedProduct(productId);

        return switch (product.getType()) {
            case COURSE -> createCourseSection(product.getId(), dto);
            case DOWNLOAD -> createDownloadSection(product.getId(), dto);
            case CONSULTATION -> throw unsupported("Sections are not supported for CONSULTATION products.");
        };
    }

    @Transactional
    public ProductSectionResponseDto updateSection(
            String productId,
            String sectionId,
            ProductSectionUpdateRequestDto dto
    ) {
        Product product = getOwnedProduct(productId);

        return switch (product.getType()) {
            case COURSE -> updateCourseSection(product.getId(), sectionId, dto);
            case DOWNLOAD -> updateDownloadSection(product.getId(), sectionId, dto);
            case CONSULTATION -> throw unsupported("Sections are not supported for CONSULTATION products.");
        };
    }

    @Transactional
    public void deleteSection(String productId, String sectionId) {
        Product product = getOwnedProduct(productId);

        switch (product.getType()) {
            case COURSE -> deleteCourseSection(product.getId(), sectionId);
            case DOWNLOAD -> deleteDownloadSection(product.getId(), sectionId);
            case CONSULTATION -> throw unsupported("Sections are not supported for CONSULTATION products.");
        }
    }

    @Transactional(readOnly = true)
    public ProductSectionResponseDto getSection(String productId, String sectionId) {
        Product product = getOwnedProduct(productId);

        return switch (product.getType()) {
            case COURSE -> mapCourseSection(loadCourseSection(product.getId(), sectionId));
            case DOWNLOAD -> mapDownloadSection(loadDownloadSection(product.getId(), sectionId));
            case CONSULTATION -> throw unsupported("Sections are not supported for CONSULTATION products.");
        };
    }

    private ProductSectionResponseDto createCourseSection(UUID productId, ProductSectionCreateRequestDto dto) {
        CourseProduct course = courseProductRepository.findFullById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + productId));

        CourseSection section = new CourseSection();
        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());
        section.setPosition(resolvePosition(dto.getPosition(), course.getSections().size()));
        section.setCourse(course);

        course.getSections().add(section);
        CourseSection saved = courseSectionRepository.save(section);
        touchCourse(course);
        return mapCourseSection(saved);
    }

    private ProductSectionResponseDto updateCourseSection(
            UUID productId,
            String sectionId,
            ProductSectionUpdateRequestDto dto
    ) {
        CourseSection section = loadCourseSection(productId, sectionId);

        if (dto.getTitle() != null) {
            section.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            section.setDescription(dto.getDescription());
        }
        if (dto.getPosition() != null) {
            section.setPosition(dto.getPosition());
        }

        CourseSection saved = courseSectionRepository.save(section);
        touchCourse(section.getCourse());
        return mapCourseSection(saved);
    }

    private void deleteCourseSection(UUID productId, String sectionId) {
        CourseSection section = loadCourseSection(productId, sectionId);
        CourseProduct course = section.getCourse();
        courseSectionRepository.delete(section);
        touchCourse(course);
    }

    private ProductSectionResponseDto createDownloadSection(UUID productId, ProductSectionCreateRequestDto dto) {
        DownloadProduct product = downloadProductRepository.findFullById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Download product not found: " + productId));

        SectionDownloadProduct section = new SectionDownloadProduct();
        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());
        section.setPosition(resolvePosition(dto.getPosition(), product.getSectionDownloadProducts().size()));
        section.setDownloadProduct(product);

        product.getSectionDownloadProducts().add(section);
        SectionDownloadProduct saved = downloadSectionRepository.save(section);
        touchDownloadProduct(product);
        return mapDownloadSection(saved);
    }

    private ProductSectionResponseDto updateDownloadSection(
            UUID productId,
            String sectionId,
            ProductSectionUpdateRequestDto dto
    ) {
        SectionDownloadProduct section = loadDownloadSection(productId, sectionId);

        if (dto.getTitle() != null) {
            section.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            section.setDescription(dto.getDescription());
        }
        if (dto.getPosition() != null) {
            section.setPosition(dto.getPosition());
        }

        SectionDownloadProduct saved = downloadSectionRepository.save(section);
        touchDownloadProduct(section.getDownloadProduct());
        return mapDownloadSection(saved);
    }

    private void deleteDownloadSection(UUID productId, String sectionId) {
        SectionDownloadProduct section = loadDownloadSection(productId, sectionId);
        DownloadProduct product = section.getDownloadProduct();
        downloadSectionRepository.delete(section);
        touchDownloadProduct(product);
    }

    private CourseSection loadCourseSection(UUID productId, String sectionId) {
        CourseSection section = courseSectionRepository.findById(UUID.fromString(sectionId))
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        if (!section.getCourse().getId().equals(productId)) {
            throw new ResourceNotFoundException(
                    "Section not found for course " + productId + ": " + sectionId
            );
        }

        return section;
    }

    private SectionDownloadProduct loadDownloadSection(UUID productId, String sectionId) {
        SectionDownloadProduct section = downloadSectionRepository.findById(UUID.fromString(sectionId))
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        if (!section.getDownloadProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException(
                    "Section not found for download product " + productId + ": " + sectionId
            );
        }

        return section;
    }

    private Product getOwnedProduct(String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!product.getUser().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You don’t own this product.");
        }

        return product;
    }

    private ProductSectionResponseDto mapCourseSection(CourseSection section) {
        ProductSectionResponseDto dto = new ProductSectionResponseDto();
        dto.setId(section.getId().toString());
        dto.setProductId(section.getCourse().getId().toString());
        dto.setTitle(section.getTitle());
        dto.setDescription(section.getDescription());
        dto.setPosition(section.getPosition());
        dto.setLessons(section.getLessons() == null
                ? List.of()
                : section.getLessons().stream()
                .sorted(Comparator.comparing(CourseLesson::getPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(this::mapLesson)
                .toList());
        return dto;
    }

    private ProductSectionResponseDto mapDownloadSection(SectionDownloadProduct section) {
        ProductSectionResponseDto dto = new ProductSectionResponseDto();
        dto.setId(section.getId().toString());
        dto.setProductId(section.getDownloadProduct().getId().toString());
        dto.setTitle(section.getTitle());
        dto.setDescription(section.getDescription());
        dto.setPosition(section.getPosition());
        dto.setFiles(section.getFiles() == null
                ? List.of()
                : section.getFiles().stream()
                .sorted(Comparator.comparing(FileDownloadProduct::getUploadedAt, Comparator.nullsLast(LocalDateTime::compareTo)))
                .map(this::mapFile)
                .toList());
        return dto;
    }

    private CourseLessonResponseDto mapLesson(CourseLesson lesson) {
        CourseLessonResponseDto dto = new CourseLessonResponseDto();
        dto.setId(lesson.getId().toString());
        dto.setTitle(lesson.getTitle());
        dto.setType(lesson.getType().name());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setContent(lesson.getContent());
        dto.setDescription(lesson.getDescription());
        dto.setPosition(lesson.getPosition());
        dto.setSectionId(lesson.getSection().getId().toString());
        return dto;
    }

    private FileDownloadProductResponseDto mapFile(FileDownloadProduct file) {
        FileDownloadProductResponseDto dto = new FileDownloadProductResponseDto();
        dto.setId(file.getId());
        dto.setFileName(file.getFileName());
        dto.setSize(file.getSize());
        dto.setFileType(file.getFileType());
        dto.setUrl(String.format("%s/%s/%s", cdnEndpointUrl, mediaBucket, file.getPath()));
        return dto;
    }

    private void touchCourse(CourseProduct course) {
        course.setUpdatedAt(LocalDateTime.now());
        courseProductRepository.save(course);
    }

    private void touchDownloadProduct(DownloadProduct product) {
        product.setUpdatedAt(LocalDateTime.now());
        downloadProductRepository.save(product);
    }

    private int resolvePosition(Integer requestedPosition, int existingCount) {
        return requestedPosition != null ? requestedPosition : existingCount + 1;
    }

    private UnsupportedProductOperationException unsupported(String message) {
        return new UnsupportedProductOperationException(message);
    }
}

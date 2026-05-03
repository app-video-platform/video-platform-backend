package com.myproject.video.video_platform.service.product.course;

import com.myproject.video.video_platform.common.enums.products.course.LessonType;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.products.authoring.ProductLessonCreateRequestDto;
import com.myproject.video.video_platform.dto.products.authoring.ProductLessonUpdateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonUpdateRequestDto;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.product.UnsupportedProductOperationException;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseLessonService {

    private final ProductRepository productRepository;
    private final CourseProductRepository courseRepo;
    private final CourseSectionRepository sectionRepo;
    private final CourseLessonRepository lessonRepo;
    private final CurrentUserService currentUserService;

    /**
     * POST /api/sections/{sectionId}/lessons
     */
    @Transactional
    public CourseLessonResponseDto createLesson(CourseLessonCreateRequestDto dto) {

        UUID sectionId = UUID.fromString(dto.getSectionId());
        CourseSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!section.getCourse().getUser().getUserId().equals(currentUserId))
            throw new AccessDeniedException("You don’t own this product.");


        CourseLesson lesson = new CourseLesson();
        lesson.setTitle(dto.getTitle());
        LessonType lessonType = LessonType.valueOf(dto.getType().toUpperCase());
        lesson.setType(lessonType);
        applyLessonContent(lesson, lessonType, dto.getVideoUrl(), dto.getContent());
        lesson.setDescription(dto.getDescription());
        lesson.setPosition(
                dto.getPosition() == null
                        ? section.getLessons().size() + 1
                        : dto.getPosition()
        );
        lesson.setSection(section);

        section.getLessons().add(lesson);
        lessonRepo.save(lesson); // cascade saves lesson
        touchProductUpdatedAt(section.getCourse());

        CourseLessonResponseDto resp = new CourseLessonResponseDto();
        resp.setId(lesson.getId().toString());
        resp.setTitle(lesson.getTitle());
        resp.setType(lesson.getType().name());
        resp.setVideoUrl(lesson.getVideoUrl());
        resp.setContent(lesson.getContent());
        resp.setDescription(lesson.getDescription());
        resp.setPosition(lesson.getPosition());
        resp.setSectionId(section.getId().toString());
        return resp;
    }

    /**
     * PUT /api/lessons/{lessonId}
     */
    @Transactional
    public void updateLesson(CourseLessonUpdateRequestDto dto) {
        UUID lessonId = UUID.fromString(dto.getId());
        CourseLesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + dto.getId()));

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!lesson.getSection().getCourse().getUser().getUserId().equals(currentUserId))
            throw new AccessDeniedException("You don’t own this product.");

        lesson.setTitle(dto.getTitle());
        LessonType newType = LessonType.valueOf(dto.getType().toUpperCase());
        lesson.setType(newType);
        applyLessonContent(lesson, newType, dto.getVideoUrl(), dto.getContent());
        lesson.setDescription(dto.getDescription());
        if (dto.getPosition() != null) {
            lesson.setPosition(dto.getPosition());
        }
        lessonRepo.save(lesson);
        touchProductUpdatedAt(lesson.getSection().getCourse());
    }

    @Transactional
    public CourseLessonResponseDto createLesson(
            String productId,
            String sectionId,
            ProductLessonCreateRequestDto dto
    ) {
        Product product = loadOwnedProduct(productId);
        if (product.getType() != ProductType.COURSE) {
            throw new UnsupportedProductOperationException("Lessons are only supported for COURSE products.");
        }

        CourseSection section = loadOwnedSection(product.getId(), sectionId);

        CourseLesson lesson = new CourseLesson();
        lesson.setTitle(dto.getTitle());
        LessonType lessonType = LessonType.valueOf(dto.getType().toUpperCase());
        lesson.setType(lessonType);
        applyLessonContent(lesson, lessonType, dto.getVideoUrl(), dto.getContent());
        lesson.setDescription(dto.getDescription());
        lesson.setPosition(
                dto.getPosition() == null
                        ? section.getLessons().size() + 1
                        : dto.getPosition()
        );
        lesson.setSection(section);

        section.getLessons().add(lesson);
        CourseLesson saved = lessonRepo.save(lesson);
        touchProductUpdatedAt(section.getCourse());
        return mapLesson(saved);
    }

    @Transactional
    public CourseLessonResponseDto updateLesson(
            String productId,
            String sectionId,
            String lessonId,
            ProductLessonUpdateRequestDto dto
    ) {
        Product product = loadOwnedProduct(productId);
        if (product.getType() != ProductType.COURSE) {
            throw new UnsupportedProductOperationException("Lessons are only supported for COURSE products.");
        }

        CourseSection section = loadOwnedSection(product.getId(), sectionId);
        CourseLesson lesson = loadLesson(section.getId(), lessonId);

        if (dto.getTitle() != null) {
            lesson.setTitle(dto.getTitle());
        }

        LessonType effectiveType = lesson.getType();
        if (dto.getType() != null) {
            effectiveType = LessonType.valueOf(dto.getType().toUpperCase());
            lesson.setType(effectiveType);
            applyLessonContent(lesson, effectiveType, dto.getVideoUrl(), dto.getContent());
        } else if (effectiveType == LessonType.VIDEO && dto.getVideoUrl() != null) {
            lesson.setVideoUrl(dto.getVideoUrl());
        } else if (effectiveType == LessonType.ARTICLE && dto.getContent() != null) {
            lesson.setContent(dto.getContent());
        }

        if (dto.getDescription() != null) {
            lesson.setDescription(dto.getDescription());
        }
        if (dto.getPosition() != null) {
            lesson.setPosition(dto.getPosition());
        }

        CourseLesson saved = lessonRepo.save(lesson);
        touchProductUpdatedAt(section.getCourse());
        return mapLesson(saved);
    }

    @Transactional
    public void deleteLesson(String productId, String sectionId, String lessonId) {
        Product product = loadOwnedProduct(productId);
        if (product.getType() != ProductType.COURSE) {
            throw new UnsupportedProductOperationException("Lessons are only supported for COURSE products.");
        }

        CourseSection section = loadOwnedSection(product.getId(), sectionId);
        CourseLesson lesson = loadLesson(section.getId(), lessonId);

        CourseProduct course = lesson.getSection().getCourse();
        lessonRepo.delete(lesson);
        touchProductUpdatedAt(course);
    }

public void deleteLesson(String userId, String lessonId) {
        Optional<CourseLesson> lessonOptional = lessonRepo.findById(UUID.fromString(lessonId));
        if (lessonOptional.isPresent()) {
            UUID currentUserId = currentUserService.getCurrentUserId();
            log.info("User id from context: {}", currentUserId);

            if (!lessonOptional.get().getSection().getCourse().getUser().getUserId().equals(currentUserId))
                throw new AccessDeniedException("You don’t own this product.");

            CourseProduct course = lessonOptional.get().getSection().getCourse();
            lessonRepo.delete(lessonOptional.get());
            touchProductUpdatedAt(course);
            log.info("Deleted succesfully a Course lesson: {}", lessonId);
        } else
            throw new ResourceNotFoundException("Course lesson not found for ID: " + lessonId);

}

    private Product loadOwnedProduct(String productId) {
        Product product = productRepository.findById(UUID.fromString(productId))
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!product.getUser().getUserId().equals(currentUserId)) {
            throw new AccessDeniedException("You don’t own this product.");
        }
        return product;
    }

    private CourseSection loadOwnedSection(UUID productId, String sectionId) {
        CourseSection section = sectionRepo.findById(UUID.fromString(sectionId))
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        if (!section.getCourse().getId().equals(productId)) {
            throw new ResourceNotFoundException(
                    "Section not found for course " + productId + ": " + sectionId
            );
        }
        return section;
    }

    private CourseLesson loadLesson(UUID sectionId, String lessonId) {
        CourseLesson lesson = lessonRepo.findById(UUID.fromString(lessonId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        if (!lesson.getSection().getId().equals(sectionId)) {
            throw new ResourceNotFoundException(
                    "Lesson not found for section " + sectionId + ": " + lessonId
            );
        }
        return lesson;
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

    private void applyLessonContent(CourseLesson lesson, LessonType type, String videoUrl, String content) {
        switch (type) {
            case VIDEO -> {
                lesson.setVideoUrl(videoUrl);
                lesson.setContent(null);
            }
            case ARTICLE -> {
                lesson.setContent(content);
                lesson.setVideoUrl(null);
            }
            case QUIZ -> {
                lesson.setVideoUrl(null);
                lesson.setContent(null);
            }
        }
    }

    private void touchProductUpdatedAt(CourseProduct course) {
        course.setUpdatedAt(LocalDateTime.now());
        courseRepo.save(course);
    }
}

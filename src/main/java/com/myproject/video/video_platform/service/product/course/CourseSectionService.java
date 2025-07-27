package com.myproject.video.video_platform.service.product.course;

import com.myproject.video.video_platform.dto.products.course.CourseSectionCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionUpdateRequestDto;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseSectionService {

    private final CourseProductRepository courseRepo;
    private final CourseSectionRepository sectionRepo;
    private final CurrentUserService currentUserService;

    /**
     * POST /api/products/{productId}/sections
     */
    @Transactional
    public CourseSectionResponseDto createSection(CourseSectionCreateRequestDto dto) {
        UUID productId = UUID.fromString(dto.getProductId());
        CourseProduct course = courseRepo.findFullById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + productId));

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);


        if (!course.getUser().getUserId().equals(currentUserId))
            throw new AccessDeniedException("You don’t own this product.");


        CourseSection section = new CourseSection();
        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());
        section.setPosition(dto.getPosition());
        section.setCourse(course);

        course.getSections().add(section);
        section = sectionRepo.save(section);

        CourseSectionResponseDto resp = new CourseSectionResponseDto();
        resp.setId(section.getId().toString());
        resp.setTitle(section.getTitle());
        resp.setDescription(section.getDescription());
        resp.setProductId(course.getId().toString());
        resp.setPosition(section.getPosition() != null ? section.getPosition() : course.getSections().size() +1);
        return resp;
    }

    /**
     * PUT /api/sections/{sectionId}
     */
    @Transactional
    public void updateSection(
            CourseSectionUpdateRequestDto dto) {

        UUID sectionId = UUID.fromString(dto.getId());
        CourseSection section = sectionRepo.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + dto.getId()));

        UUID currentUserId = currentUserService.getCurrentUserId();
        log.info("User id from context: {}", currentUserId);

        if (!section.getCourse().getUser().getUserId().equals(currentUserId))
            throw new AccessDeniedException("You don’t own this product.");

        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());

        if (dto.getPosition() != null) {
            section.setPosition(dto.getPosition());
        }
        sectionRepo.save(section);
    }

    public void deleteSection(String id) {
        Optional<CourseSection> sectionOptional = sectionRepo.findById(UUID.fromString(id));
        if (sectionOptional.isPresent()) {
            UUID currentUserId = currentUserService.getCurrentUserId();
            log.info("User id from context: {}", currentUserId);
            if (!sectionOptional.get().getCourse().getUser().getUserId().equals(currentUserId))
                throw new AccessDeniedException("You don’t own this product.");

            sectionRepo.delete(sectionOptional.get());
            log.info("Deleted succesfully a Course section: {}", id);
        } else
            throw new ResourceNotFoundException("Course section not found for ID: " + id);

    }
}

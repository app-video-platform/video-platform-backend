package com.myproject.video.video_platform.service.product.course;

import com.myproject.video.video_platform.dto.products.course.CourseSectionCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionUpdateRequestDto;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseSectionService {

    private final CourseProductRepository courseRepo;
    private final CourseSectionRepository sectionRepo;
    private final UserService userService;

    /**
     * POST /api/products/{productId}/sections
     */
    @Transactional
    public CourseSectionResponseDto createSection(
            String productIdStr,
            CourseSectionCreateRequestDto dto) {

        UUID productId = UUID.fromString(productIdStr);
        CourseProduct course = courseRepo.findFullById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + productIdStr));


        CourseSection section = new CourseSection();
        section.setTitle(dto.getTitle());
        section.setPosition(course.getSections().size() + 1);
        section.setCourse(course);

        course.getSections().add(section);
        courseRepo.save(course); // cascade persists section

        CourseSectionResponseDto resp = new CourseSectionResponseDto();
        resp.setId(section.getId());
        resp.setTitle(section.getTitle());
        resp.setPosition(section.getPosition());
        // no lessons yet
        return resp;
    }

    /**
     * PUT /api/sections/{sectionId}
     */
    @Transactional
    public CourseSectionResponseDto updateSection(
            CourseSectionUpdateRequestDto dto) {
//
//        UUID sectionId = UUID.fromString(dto.getId());
//        CourseSection section = sectionRepo.findById(sectionId)
//                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + dto.getId()));
//
//        // Ownership check:
//        String currentEmail = userService.getCurrentUserEmail();
//        if (!section.getCourse().getUser().getEmail().equalsIgnoreCase(currentEmail)) {
//            throw new AccessDeniedException("Not authorized to update section");
//        }
//
//        section.setTitle(dto.getTitle());
//        if (dto.getPosition() != null) {
//            section.setPosition(dto.getPosition());
//        }
//        CourseSection saved = sectionRepo.save(section);
//
//        CourseSectionResponseDto resp = new CourseSectionResponseDto();
//        resp.setId(saved.getId());
//        resp.setTitle(saved.getTitle());
//        resp.setPosition(saved.getPosition());
//        // We do not re-fetch lessons here; if FE needs lessons, it will GET the full course again.
//        return resp;
        return null;
    }
}

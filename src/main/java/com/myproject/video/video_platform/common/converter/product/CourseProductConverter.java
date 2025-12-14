package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductDetailsDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionResponseDto;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class CourseProductConverter {

    private static final ProductStatus DEFAULT_STATUS = ProductStatus.DRAFT;

    public CourseProduct mapCourseCreateDtoToEntity(
            CourseProductRequestDto dto,
            User owner
    ) {
        CourseProduct course = new CourseProduct();
        course.setName(dto.getName());
        course.setDescription(dto.getDescription());
        course.setType(com.myproject.video.video_platform.common.enums.products.ProductType.COURSE);
        course.setStatus(parseStatus(dto.getStatus(), DEFAULT_STATUS));
        course.setPrice(parsePrice(dto.getPrice(), BigDecimal.ZERO));
        course.setUser(owner);

        // ───► CREATE A NEW "DRAFT" SECTION BY DEFAULT ◄────────────
        CourseSection draftSection = new CourseSection();
        draftSection.setTitle("Draft");
        draftSection.setDescription("");
        draftSection.setPosition(1);
        draftSection.setCourse(course);

        // Initialize the set if null, then add the draft section
        Set<CourseSection> sections = new LinkedHashSet<>();
        sections.add(draftSection);
        course.setSections(sections);

        return course;
    }


    public CourseProductResponseDto mapCourseToResponse(CourseProduct course) {
        CourseProductResponseDto dto = new CourseProductResponseDto();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDescription(course.getDescription());
        dto.setUserId(course.getUser().getUserId());
        dto.setType(course.getType().name());
        dto.setStatus(course.getStatus().toString());
        dto.setPrice(
                course.getPrice() == null
                        ? "0"
                        : course.getPrice().compareTo(BigDecimal.ZERO) == 0
                        ? "free"
                        : course.getPrice().toString()
        );


        if (course.getSections() != null) {
            List<CourseSectionResponseDto> sectionDtos = course.getSections().stream()
                    .sorted(Comparator.comparing(CourseSection::getPosition))
                    .map(this::mapSection)
                    .toList();
            CourseProductDetailsDto details = new CourseProductDetailsDto();
            details.setSections(sectionDtos);
            dto.setDetails(details);
        }
        return dto;
    }

    private CourseSectionResponseDto mapSection(CourseSection section) {
        CourseSectionResponseDto secDto = new CourseSectionResponseDto();
        secDto.setId(section.getId().toString());
        secDto.setTitle(section.getTitle());
        secDto.setPosition(section.getPosition());

        if (section.getLessons() != null) {
            List<CourseLessonResponseDto> lessonDtos = section.getLessons().stream()
                    .sorted(Comparator.comparing(CourseLesson::getPosition))
                    .map(this::mapLessonLazyLoad)
                    .toList();
            secDto.setLessons(lessonDtos);
        }
        return secDto;
    }

    private CourseLessonResponseDto mapLessonLazyLoad(CourseLesson lesson) {
        CourseLessonResponseDto dto = new CourseLessonResponseDto();
        dto.setId(lesson.getId().toString());
        dto.setTitle(lesson.getTitle());
        dto.setType(lesson.getType().name());
        dto.setPosition(lesson.getPosition());
        return dto;
    }

    public void applyCourseUpdateDto(CourseProduct existing, CourseProductRequestDto dto) {
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(parseStatus(dto.getStatus(), existing.getStatus() != null ? existing.getStatus() : DEFAULT_STATUS));
        }
        if (dto.getPrice() != null) {
            existing.setPrice(parsePrice(dto.getPrice(), existing.getPrice() != null ? existing.getPrice() : BigDecimal.ZERO));
        }
    }

    private ProductStatus parseStatus(String statusStr, ProductStatus fallback) {
        if (statusStr == null || statusStr.isBlank()) {
            return fallback;
        }
        try {
            return ProductStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private BigDecimal parsePrice(String priceStr, BigDecimal fallback) {
        if (priceStr == null || priceStr.isBlank()) {
            return fallback;
        }
        if (priceStr.equalsIgnoreCase("free")) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(priceStr);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionResponseDto;
import com.myproject.video.video_platform.entity.auth.User;
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


    public CourseProduct mapCourseCreateDtoToEntity(
            CourseProductRequestDto dto,
            User owner
    ) {
        CourseProduct course = new CourseProduct();
        course.setName(dto.getName());
        course.setDescription(dto.getDescription());
        course.setType(com.myproject.video.video_platform.common.enums.products.ProductType.COURSE);
        course.setStatus(ProductStatus.valueOf(dto.getStatus().toUpperCase()));
        course.setPrice(
                dto.getPrice() == null
                        ? BigDecimal.ZERO
                        : dto.getPrice().equalsIgnoreCase("free")
                        ? BigDecimal.ZERO
                        : new BigDecimal(dto.getPrice())
        );
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
            dto.setSections(sectionDtos);
        }
        return dto;
    }

    private CourseSectionResponseDto mapSection(CourseSection section) {
        CourseSectionResponseDto secDto = new CourseSectionResponseDto();
        secDto.setId(section.getId());
        secDto.setTitle(section.getTitle());
        secDto.setPosition(section.getPosition());

        if (section.getLessons() != null) {
            List<CourseLessonResponseDto> lessonDtos = section.getLessons().stream()
                    .sorted(Comparator.comparing(CourseLesson::getPosition))
                    .map(this::mapLesson)
                    .toList();
            secDto.setLessons(lessonDtos);
        }
        return secDto;
    }

    private CourseLessonResponseDto mapLesson(CourseLesson lesson) {
        CourseLessonResponseDto dto = new CourseLessonResponseDto();
        dto.setId(lesson.getId());
        dto.setTitle(lesson.getTitle());
        dto.setType(lesson.getType().name());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setContent(lesson.getContent());
        dto.setPosition(lesson.getPosition());
        return dto;
    }
}

package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.common.enums.products.course.LessonType;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductResponseDto;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import com.myproject.video.video_platform.entity.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseProductConverterTest {

    private final CourseProductConverter converter = new CourseProductConverter();

    @Test
    void mapCourseCreateDtoToEntity_defaultsStatusPriceAndCreatesDraftSection() {
        User owner = new User();
        owner.setUserId(UUID.randomUUID());

        CourseProductRequestDto dto = new CourseProductRequestDto();
        dto.setName("Course");
        dto.setDescription("Desc");
        dto.setStatus(null);
        dto.setPrice(null);

        CourseProduct course = converter.mapCourseCreateDtoToEntity(dto, owner);

        assertEquals("Course", course.getName());
        assertEquals("Desc", course.getDescription());
        assertEquals(ProductType.COURSE, course.getType());
        assertEquals(ProductStatus.DRAFT, course.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(course.getPrice()));
        assertSame(owner, course.getUser());

        assertNotNull(course.getSections());
        assertEquals(1, course.getSections().size());
        CourseSection section = course.getSections().iterator().next();
        assertEquals("Draft", section.getTitle());
        assertEquals("", section.getDescription());
        assertEquals(1, section.getPosition());
        assertSame(course, section.getCourse());
    }

    @Test
    void applyCourseUpdateDto_onlyUpdatesProvidedFields() {
        CourseProduct existing = new CourseProduct();
        existing.setName("Old");
        existing.setDescription("Old desc");
        existing.setType(ProductType.COURSE);
        existing.setStatus(ProductStatus.PUBLISHED);
        existing.setPrice(BigDecimal.valueOf(99));

        CourseProductRequestDto dto = new CourseProductRequestDto();
        dto.setName(null);
        dto.setDescription("New desc");
        dto.setStatus(null);
        dto.setPrice("free");

        converter.applyCourseUpdateDto(existing, dto);

        assertEquals("Old", existing.getName(), "Null name must not overwrite existing");
        assertEquals("New desc", existing.getDescription());
        assertEquals(ProductStatus.PUBLISHED, existing.getStatus(), "Null status must not overwrite existing");
        assertEquals(0, BigDecimal.ZERO.compareTo(existing.getPrice()));
    }

    @Test
    void mapCourseToResponse_putsSectionsUnderDetailsAndMapsLessonSummaries() {
        User owner = new User();
        owner.setUserId(UUID.randomUUID());

        CourseProduct course = new CourseProduct();
        course.setId(UUID.randomUUID());
        course.setName("Course");
        course.setDescription("Desc");
        course.setType(ProductType.COURSE);
        course.setStatus(ProductStatus.PUBLISHED);
        course.setPrice(BigDecimal.TEN);
        course.setUser(owner);

        CourseSection section = new CourseSection();
        section.setId(UUID.randomUUID());
        section.setTitle("Section");
        section.setDescription("S desc");
        section.setPosition(2);
        section.setCourse(course);

        CourseLesson lesson = new CourseLesson();
        lesson.setId(UUID.randomUUID());
        lesson.setTitle("Lesson");
        lesson.setType(LessonType.VIDEO);
        lesson.setPosition(1);
        lesson.setSection(section);
        section.setLessons(Set.of(lesson));

        course.setSections(Set.of(section));

        CourseProductResponseDto dto = converter.mapCourseToResponse(course);

        assertEquals("COURSE", dto.getType());
        assertNotNull(dto.getDetails());
        assertNotNull(dto.getDetails().getSections());
        assertEquals(1, dto.getDetails().getSections().size());
        assertEquals("Section", dto.getDetails().getSections().get(0).getTitle());
        assertEquals(1, dto.getDetails().getSections().get(0).getLessons().size());
        assertEquals("Lesson", dto.getDetails().getSections().get(0).getLessons().get(0).getTitle());
        assertEquals("VIDEO", dto.getDetails().getSections().get(0).getLessons().get(0).getType());
        assertNull(dto.getDetails().getSections().get(0).getLessons().get(0).getVideoUrl(), "Course response maps lesson summaries only");
        assertNull(dto.getDetails().getSections().get(0).getLessons().get(0).getContent(), "Course response maps lesson summaries only");
        assertTrue(dto.getPrice().equals("10") || dto.getPrice().equals("10.0") || dto.getPrice().equals("10.00"));
    }
}


package com.myproject.video.video_platform.common.converter.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductResponseDto;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CourseProductConverterTest {

    private CourseProductConverter converter;

    @BeforeEach
    void setup() {
        converter = new CourseProductConverter();
    }

    @Test
    void mapCourseCreateDtoToEntity_createsDraftSection() {
        CourseProductRequestDto dto = new CourseProductRequestDto();
        dto.setName("Course");
        dto.setDescription("desc");
        dto.setStatus("draft");
        dto.setPrice("free");

        User user = new User();
        user.setUserId(UUID.randomUUID());

        CourseProduct course = converter.mapCourseCreateDtoToEntity(dto, user);

        assertEquals("Course", course.getName());
        assertEquals(ProductStatus.DRAFT, course.getStatus());
        assertEquals(BigDecimal.ZERO, course.getPrice());
        assertEquals(user, course.getUser());
        assertNotNull(course.getSections());
        assertEquals(1, course.getSections().size());
        CourseSection section = course.getSections().iterator().next();
        assertEquals("Draft", section.getTitle());
        assertEquals(Integer.valueOf(1), section.getPosition());
    }

    @Test
    void mapCourseToResponse_sortsSections() {
        User user = new User();
        user.setUserId(UUID.randomUUID());

        CourseProduct course = new CourseProduct();
        course.setId(UUID.randomUUID());
        course.setName("Course");
        course.setDescription("desc");
        course.setStatus(ProductStatus.PUBLISHED);
        course.setPrice(new BigDecimal("1"));
        course.setUser(user);
        course.setType(com.myproject.video.video_platform.common.enums.products.ProductType.COURSE);

        CourseSection sec2 = new CourseSection();
        sec2.setId(UUID.randomUUID());
        sec2.setTitle("two");
        sec2.setPosition(2);
        sec2.setCourse(course);

        CourseSection sec1 = new CourseSection();
        sec1.setId(UUID.randomUUID());
        sec1.setTitle("one");
        sec1.setPosition(1);
        sec1.setCourse(course);

        course.setSections(new LinkedHashSet<>());
        course.getSections().add(sec2);
        course.getSections().add(sec1);

        CourseProductResponseDto resp = converter.mapCourseToResponse(course);

        assertEquals(2, resp.getSections().size());
        assertEquals("one", resp.getSections().get(0).getTitle());
        assertEquals("two", resp.getSections().get(1).getTitle());
    }

    @Test
    void applyCourseUpdateDto_updatesFields() {
        CourseProduct existing = new CourseProduct();
        existing.setPrice(BigDecimal.ZERO);
        existing.setStatus(ProductStatus.DRAFT);

        CourseProductRequestDto dto = new CourseProductRequestDto();
        dto.setName("Updated");
        dto.setDescription("new");
        dto.setStatus("PUBLISHED");
        dto.setPrice("10");

        converter.applyCourseUpdateDto(existing, dto);

        assertEquals("Updated", existing.getName());
        assertEquals("new", existing.getDescription());
        assertEquals(ProductStatus.PUBLISHED, existing.getStatus());
        assertEquals(new BigDecimal("10"), existing.getPrice());
    }
}

package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.dto.products.course.CourseLessonCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonUpdateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionUpdateRequestDto;
import com.myproject.video.video_platform.service.product.course.CourseLessonService;
import com.myproject.video.video_platform.service.product.course.CourseSectionService;
import com.myproject.video.video_platform.service.product.strategy_handler.CourseProductHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class CourseController {

    private final CourseProductHandler courseHandler;
    private final CourseSectionService sectionService;
    private final CourseLessonService lessonService;

    //
    // (1) & (2) are already in ProductController, so we only need (3)-(6) here:
    //

    // (3) Create a Section: POST /api/products/{productId}/sections
    @PostMapping("/products/{productId}/sections")
    public ResponseEntity<CourseSectionResponseDto> createSection(
            @PathVariable String productId,
            @Validated @RequestBody CourseSectionCreateRequestDto dto) {

        CourseSectionResponseDto resp = sectionService.createSection(productId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // (4) Update a Section: PUT /api/sections/{sectionId}
    @PutMapping("/sections/{sectionId}")
    public ResponseEntity<CourseSectionResponseDto> updateSection(
            @PathVariable String sectionId,
            @Validated @RequestBody CourseSectionUpdateRequestDto dto) {

        dto.setId(sectionId);
        CourseSectionResponseDto resp = sectionService.updateSection(dto);
        return ResponseEntity.ok(resp);
    }

    // (5) Create a Lesson: POST /api/sections/{sectionId}/lessons
    @PostMapping("/sections/{sectionId}/lessons")
    public ResponseEntity<CourseLessonResponseDto> createLesson(
            @PathVariable String sectionId,
            @Validated @RequestBody CourseLessonCreateRequestDto dto) {

        CourseLessonResponseDto resp = lessonService.createLesson(sectionId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // (6) Update a Lesson: PUT /api/lessons/{lessonId}
    @PutMapping("/lessons/{lessonId}")
    public ResponseEntity<CourseLessonResponseDto> updateLesson(
            @PathVariable String lessonId,
            @Validated @RequestBody CourseLessonUpdateRequestDto dto) {

        dto.setId(lessonId);
        CourseLessonResponseDto resp = lessonService.updateLesson(dto);
        return ResponseEntity.ok(resp);
    }
}

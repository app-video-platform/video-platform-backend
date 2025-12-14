package com.myproject.video.video_platform.controller.product.course;

import com.myproject.video.video_platform.controller.docs.product.course.CourseContentApiDoc;
import com.myproject.video.video_platform.dto.products.course.CourseLessonCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonUpdateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionUpdateRequestDto;
import com.myproject.video.video_platform.service.product.course.CourseLessonService;
import com.myproject.video.video_platform.service.product.course.CourseSectionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/products/course")
@RequiredArgsConstructor
@Validated
@Tag(name = "Courses", description = "Manage course sections and lessons belonging to a course product.")
public class CourseContentController implements CourseContentApiDoc {

    private final CourseSectionService sectionService;
    private final CourseLessonService lessonService;

    @PostMapping("/section")
    @Override
    public ResponseEntity<CourseSectionResponseDto> createSection(
            @Validated @RequestBody CourseSectionCreateRequestDto dto) {

        CourseSectionResponseDto resp = sectionService.createSection(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/section")
    @Override
    public ResponseEntity<String> updateSection(
            @Validated @RequestBody CourseSectionUpdateRequestDto dto) {
        sectionService.updateSection(dto);
        return ResponseEntity.ok("Successful section update.");
    }

    @PostMapping("/section/lesson")
    @Override
    public ResponseEntity<CourseLessonResponseDto> createLesson(
            @Validated @RequestBody CourseLessonCreateRequestDto dto) {

        CourseLessonResponseDto resp = lessonService.createLesson(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/section/lesson")
    @Override
    public ResponseEntity<String> updateLesson(
            @Validated @RequestBody CourseLessonUpdateRequestDto dto) {
        lessonService.updateLesson(dto);
        return ResponseEntity.ok("Successful lesson update.");
    }

    @DeleteMapping("/section")
    @Override
    public ResponseEntity<String> deleteSection(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "id") String id) {
        sectionService.deleteSection(id);
        return ResponseEntity.ok("Successful section delete.");
    }

    @DeleteMapping("/section/lesson")
    @Override
    public ResponseEntity<String> deleteLesson(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "id") String id) {
        lessonService.deleteLesson(userId, id);
        return ResponseEntity.ok("Successful lesson delete.");
    }
}

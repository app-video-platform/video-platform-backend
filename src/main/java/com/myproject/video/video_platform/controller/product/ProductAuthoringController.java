package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.dto.products.authoring.ProductLessonCreateRequestDto;
import com.myproject.video.video_platform.dto.products.authoring.ProductLessonUpdateRequestDto;
import com.myproject.video.video_platform.dto.products.authoring.ProductSectionCreateRequestDto;
import com.myproject.video.video_platform.dto.products.authoring.ProductSectionResponseDto;
import com.myproject.video.video_platform.dto.products.authoring.ProductSectionUpdateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.service.product.ProductSectionService;
import com.myproject.video.video_platform.service.product.course.CourseLessonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/products/{productId}/sections")
@Tag(name = "Product Authoring", description = "Canonical nested authoring endpoints for product sections and course lessons.")
public class ProductAuthoringController {

    private final ProductSectionService productSectionService;
    private final CourseLessonService courseLessonService;

    @PostMapping
    public ResponseEntity<ProductSectionResponseDto> createSection(
            @PathVariable("productId") String productId,
            @Valid @RequestBody ProductSectionCreateRequestDto request
    ) {
        ProductSectionResponseDto response = productSectionService.createSection(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{sectionId}")
    public ResponseEntity<ProductSectionResponseDto> updateSection(
            @PathVariable("productId") String productId,
            @PathVariable("sectionId") String sectionId,
            @RequestBody ProductSectionUpdateRequestDto request
    ) {
        ProductSectionResponseDto response = productSectionService.updateSection(productId, sectionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<Void> deleteSection(
            @PathVariable("productId") String productId,
            @PathVariable("sectionId") String sectionId
    ) {
        productSectionService.deleteSection(productId, sectionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sectionId}/lessons")
    public ResponseEntity<CourseLessonResponseDto> createLesson(
            @PathVariable("productId") String productId,
            @PathVariable("sectionId") String sectionId,
            @Valid @RequestBody ProductLessonCreateRequestDto request
    ) {
        CourseLessonResponseDto response = courseLessonService.createLesson(productId, sectionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<CourseLessonResponseDto> updateLesson(
            @PathVariable("productId") String productId,
            @PathVariable("sectionId") String sectionId,
            @PathVariable("lessonId") String lessonId,
            @RequestBody ProductLessonUpdateRequestDto request
    ) {
        CourseLessonResponseDto response = courseLessonService.updateLesson(productId, sectionId, lessonId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sectionId}/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable("productId") String productId,
            @PathVariable("sectionId") String sectionId,
            @PathVariable("lessonId") String lessonId
    ) {
        courseLessonService.deleteLesson(productId, sectionId, lessonId);
        return ResponseEntity.noContent().build();
    }
}

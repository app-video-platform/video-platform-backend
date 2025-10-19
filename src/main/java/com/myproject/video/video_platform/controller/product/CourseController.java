package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.products.course.CourseLessonCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonUpdateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionResponseDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionUpdateRequestDto;
import com.myproject.video.video_platform.service.product.course.CourseLessonService;
import com.myproject.video.video_platform.service.product.course.CourseSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class CourseController {

    private final CourseSectionService sectionService;
    private final CourseLessonService lessonService;

    @PostMapping("/section")
    @Operation(summary = "Create course section", description = "Appends a new section to the specified course. Only the owning teacher may perform this action.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Section created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CourseSectionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the course",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CourseSectionResponseDto> createSection(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "New section definition",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CourseSectionCreateRequestDto.class)))
            @Validated @RequestBody CourseSectionCreateRequestDto dto) {

        CourseSectionResponseDto resp = sectionService.createSection(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/section")
    @Operation(summary = "Update course section", description = "Updates metadata of a course section owned by the authenticated teacher.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Section updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Successful section update.\""))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the course",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Section not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> updateSection(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated section fields",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CourseSectionUpdateRequestDto.class)))
            @Validated @RequestBody CourseSectionUpdateRequestDto dto) {
        sectionService.updateSection(dto);
        return ResponseEntity.ok("Successful section update.");
    }

    @PostMapping("/section/lesson")
    @Operation(summary = "Create lesson", description = "Adds a lesson to a course section. Ownership is enforced using the userId field.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lesson created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CourseLessonResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the course",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Section not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CourseLessonResponseDto> createLesson(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the lesson to append",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CourseLessonCreateRequestDto.class)))
            @Validated @RequestBody CourseLessonCreateRequestDto dto) {

        CourseLessonResponseDto resp = lessonService.createLesson(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PutMapping("/section/lesson")
    @Operation(summary = "Update lesson", description = "Updates lesson metadata or content. Only the owning teacher is allowed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Successful lesson update.\""))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "User does not own the course",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> updateLesson(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated lesson payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CourseLessonUpdateRequestDto.class)))
            @Validated @RequestBody CourseLessonUpdateRequestDto dto) {
        lessonService.updateLesson(dto);
        return ResponseEntity.ok("Successful lesson update.");
    }

    @DeleteMapping("/section")
    @Operation(summary = "Delete course section", description = "Deletes a course section and cascades lessons after verifying ownership.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Section deleted",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Successful section delete.\""))),
            @ApiResponse(responseCode = "403", description = "User does not own the course",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Section not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> deleteSection(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "id") String id) {
        sectionService.deleteSection(id);
        return ResponseEntity.ok("Successful section delete.");
    }

    @DeleteMapping("/section/lesson")
    @Operation(summary = "Delete lesson", description = "Removes a lesson from a section. Operation is restricted to the product owner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson deleted",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"Successful lesson delete.\""))),
            @ApiResponse(responseCode = "403", description = "User does not own the course",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<String> deleteLesson(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "id") String id) {
        lessonService.deleteLesson(userId, id);
        return ResponseEntity.ok("Successful lesson delete.");
    }
}

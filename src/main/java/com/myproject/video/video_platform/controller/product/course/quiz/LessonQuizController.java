package com.myproject.video.video_platform.controller.product.course.quiz;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionRequest;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionResponse;
import com.myproject.video.video_platform.service.product.course.quiz.LessonQuizAuthoringService;
import com.myproject.video.video_platform.service.product.course.quiz.LessonQuizPlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/lessons/{lessonId}/quiz")
@Validated
@RequiredArgsConstructor
@Tag(name = "Lesson Quizzes", description = "Manage and play quizzes attached to course lessons.")
public class LessonQuizController {

    private final LessonQuizAuthoringService authoringService;
    private final LessonQuizPlayerService playerService;

    @GetMapping
    @Operation(summary = "Get quiz for editing", description = "Returns the quiz draft for the specified lesson. Only lesson owners can call this endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuizDraftDto.class))),
            @ApiResponse(responseCode = "403", description = "Not authorized to view lesson",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson or quiz not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuizDraftDto> getQuizForAuthoring(@PathVariable("lessonId") UUID lessonId) {
        QuizDraftDto quiz = authoringService.getQuiz(lessonId);
        return ResponseEntity.ok(quiz);
    }

    @PutMapping
    @Operation(summary = "Upsert quiz", description = "Creates or updates the quiz definition attached to the lesson after validating ownership and payload rules.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz saved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuizDraftDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Not authorized to modify lesson",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuizDraftDto> upsertQuiz(
            @PathVariable("lessonId") UUID lessonId,
            @Valid @RequestBody QuizDraftDto request
    ) {
        QuizDraftDto quiz = authoringService.upsertQuiz(lessonId, request);
        return ResponseEntity.ok(quiz);
    }

    @DeleteMapping
    @Operation(summary = "Delete quiz", description = "Removes the quiz definition linked to the lesson. Only owners may delete quiz content.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Quiz deleted"),
            @ApiResponse(responseCode = "403", description = "Not authorized",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson or quiz not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteQuiz(@PathVariable("lessonId") UUID lessonId) {
        authoringService.deleteQuiz(lessonId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/play")
    @Operation(summary = "Get quiz for playing", description = "Returns the quiz definition without correct answers for learner consumption.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuizDraftDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuizDraftDto> getQuizForPlay(@PathVariable("lessonId") UUID lessonId) {
        QuizDraftDto quiz = playerService.getQuizForPlay(lessonId);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit quiz answers", description = "Scores the learner answers, persists an attempt, and returns pass/fail feedback.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Submission scored",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuizSubmissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Submission invalid",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable("lessonId") UUID lessonId,
            @Valid @RequestBody QuizSubmissionRequest request
    ) {
        QuizSubmissionResponse response = playerService.submit(lessonId, request);
        return ResponseEntity.ok(response);
    }
}

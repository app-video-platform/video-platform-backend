package com.myproject.video.video_platform.controller.docs.product.course.quiz;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionRequest;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface LessonQuizApiDoc {

    @Operation(
            summary = "Get quiz for editing",
            description = "Returns the quiz draft for the specified lesson. Only lesson owners can call this endpoint."
    )
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
    ResponseEntity<QuizDraftDto> getQuizForAuthoring(UUID lessonId);

    @Operation(
            summary = "Upsert quiz",
            description = "Creates or updates the quiz definition attached to the lesson after validating ownership and payload rules."
    )
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
    ResponseEntity<QuizDraftDto> upsertQuiz(UUID lessonId, @Valid QuizDraftDto request);

    @Operation(
            summary = "Delete quiz",
            description = "Removes the quiz definition linked to the lesson. Only owners may delete quiz content."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Quiz deleted"),
            @ApiResponse(responseCode = "403", description = "Not authorized",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Lesson or quiz not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> deleteQuiz(UUID lessonId);

    @Operation(
            summary = "Get quiz for playing",
            description = "Returns the quiz definition without correct answers for learner consumption."
    )
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
    ResponseEntity<QuizDraftDto> getQuizForPlay(UUID lessonId);

    @Operation(
            summary = "Submit quiz answers",
            description = "Scores the learner answers, persists an attempt, and returns pass/fail feedback."
    )
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
    ResponseEntity<QuizSubmissionResponse> submitQuiz(UUID lessonId, @Valid QuizSubmissionRequest request);
}

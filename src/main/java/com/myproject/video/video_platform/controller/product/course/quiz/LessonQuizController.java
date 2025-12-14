package com.myproject.video.video_platform.controller.product.course.quiz;

import com.myproject.video.video_platform.controller.docs.product.course.quiz.LessonQuizApiDoc;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionRequest;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionResponse;
import com.myproject.video.video_platform.service.product.course.quiz.LessonQuizAuthoringService;
import com.myproject.video.video_platform.service.product.course.quiz.LessonQuizPlayerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class LessonQuizController implements LessonQuizApiDoc {

    private final LessonQuizAuthoringService authoringService;
    private final LessonQuizPlayerService playerService;

    @GetMapping
    @Override
    public ResponseEntity<QuizDraftDto> getQuizForAuthoring(@PathVariable("lessonId") UUID lessonId) {
        QuizDraftDto quiz = authoringService.getQuiz(lessonId);
        return ResponseEntity.ok(quiz);
    }

    @PutMapping
    @Override
    public ResponseEntity<QuizDraftDto> upsertQuiz(
            @PathVariable("lessonId") UUID lessonId,
            @Valid @RequestBody QuizDraftDto request
    ) {
        QuizDraftDto quiz = authoringService.upsertQuiz(lessonId, request);
        return ResponseEntity.ok(quiz);
    }

    @DeleteMapping
    @Override
    public ResponseEntity<Void> deleteQuiz(@PathVariable("lessonId") UUID lessonId) {
        authoringService.deleteQuiz(lessonId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/play")
    @Override
    public ResponseEntity<QuizDraftDto> getQuizForPlay(@PathVariable("lessonId") UUID lessonId) {
        QuizDraftDto quiz = playerService.getQuizForPlay(lessonId);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/submit")
    @Override
    public ResponseEntity<QuizSubmissionResponse> submitQuiz(
            @PathVariable("lessonId") UUID lessonId,
            @Valid @RequestBody QuizSubmissionRequest request
    ) {
        QuizSubmissionResponse response = playerService.submit(lessonId, request);
        return ResponseEntity.ok(response);
    }
}

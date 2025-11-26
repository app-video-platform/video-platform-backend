package com.myproject.video.video_platform.service.product.course.quiz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.course.LessonType;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizQuestionResultDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionRequest;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionResponse;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.quiz.Quiz;
import com.myproject.video.video_platform.entity.products.course.quiz.QuizAttempt;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.quiz.QuizAttemptRepository;
import com.myproject.video.video_platform.service.product.course.quiz.QuizSubmissionEvaluator.QuestionResult;
import com.myproject.video.video_platform.service.product.course.quiz.QuizSubmissionEvaluator.QuizSubmissionEvaluation;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonQuizPlayerService {

    private final CourseLessonRepository lessonRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final QuizMapper quizMapper;
    private final QuizSubmissionEvaluator submissionEvaluator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public QuizDraftDto getQuizForPlay(UUID lessonId) {
        CourseLesson lesson = requireLesson(lessonId);
        ensureQuizLesson(lesson);
        ensureLearnerAccess(lesson);
        Quiz quiz = requireQuiz(lesson);
        return quizMapper.toPlayerDto(quiz);
    }

    @Transactional
    public QuizSubmissionResponse submit(UUID lessonId, QuizSubmissionRequest request) {
        CourseLesson lesson = requireLesson(lessonId);
        ensureQuizLesson(lesson);
        ensureLearnerAccess(lesson);
        Quiz quiz = requireQuiz(lesson);

        QuizSubmissionEvaluation evaluation = submissionEvaluator.evaluate(quiz, request);
        User user = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setLesson(lesson);
        attempt.setUser(user);
        attempt.setSubmittedAt(Instant.now());
        attempt.setTotalPointsPossible(evaluation.totalPointsPossible());
        attempt.setTotalPointsAchieved(evaluation.totalPointsAchieved());
        attempt.setPercentage(evaluation.percentage());
        attempt.setPassed(evaluation.passed());
        attempt.setAnswersJson(toJson(evaluation.normalizedAnswers()));
        attemptRepository.save(attempt);

        return buildSubmissionResponse(lesson, quiz, evaluation);
    }

    private QuizSubmissionResponse buildSubmissionResponse(CourseLesson lesson, Quiz quiz, QuizSubmissionEvaluation evaluation) {
        QuizSubmissionResponse response = new QuizSubmissionResponse();
        response.setLessonId(lesson.getId().toString());
        response.setQuizId(quiz.getId().toString());
        response.setTotalPointsPossible(evaluation.totalPointsPossible());
        response.setTotalPointsAchieved(evaluation.totalPointsAchieved());
        response.setPercentage(evaluation.percentage());
        response.setPassed(evaluation.passed());
        response.setPassingScore(quiz.getPassingScore());

        List<QuizQuestionResultDto> questions = evaluation.questionResults().stream()
                .map(this::mapQuestionResult)
                .collect(Collectors.toList());
        response.setQuestions(questions);
        return response;
    }

    private QuizQuestionResultDto mapQuestionResult(QuestionResult result) {
        QuizQuestionResultDto dto = new QuizQuestionResultDto();
        dto.setQuestionId(result.questionId().toString());
        dto.setCorrect(result.correct());
        dto.setPointsAwarded(result.pointsAwarded());
        dto.setExplanation(result.explanation());
        return dto;
    }

    private String toJson(Map<UUID, List<UUID>> answers) {
        Map<String, List<String>> serializable = answers.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().stream().map(UUID::toString).toList(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        try {
            return objectMapper.writeValueAsString(serializable);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize answers", e);
        }
    }

    private CourseLesson requireLesson(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
    }

    private void ensureQuizLesson(CourseLesson lesson) {
        if (lesson.getType() != LessonType.QUIZ) {
            throw new ResourceNotFoundException("Lesson is not a quiz lesson");
        }
    }

    private void ensureLearnerAccess(CourseLesson lesson) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        UUID ownerId = lesson.getSection().getCourse().getUser().getUserId();
        if (ownerId.equals(currentUserId)) {
            return;
        }
        ProductStatus status = lesson.getSection().getCourse().getStatus();
        if (status != ProductStatus.PUBLISHED) {
            throw new AccessDeniedException("Lesson is not publicly available");
        }
    }

    private Quiz requireQuiz(CourseLesson lesson) {
        Quiz quiz = lesson.getQuiz();
        if (quiz == null) {
            throw new ResourceNotFoundException("Quiz not found for lesson " + lesson.getId());
        }
        return quiz;
    }
}

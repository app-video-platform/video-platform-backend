package com.myproject.video.video_platform.service.product.course.quiz;

import com.myproject.video.video_platform.common.enums.products.course.LessonType;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.quiz.Quiz;
import com.myproject.video.video_platform.exception.product.QuizValidationException;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.quiz.QuizRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonQuizAuthoringService {

    private final CourseLessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final CurrentUserService currentUserService;
    private final QuizValidator quizValidator;
    private final QuizMapper quizMapper;

    @Transactional(readOnly = true)
    public QuizDraftDto getQuiz(UUID lessonId) {
        CourseLesson lesson = requireLesson(lessonId);
        ensureOwner(lesson);
        ensureQuizLesson(lesson);

        Quiz quiz = lesson.getQuiz();
        if (quiz == null) {
            throw new ResourceNotFoundException("Quiz not found for lesson " + lessonId);
        }
        return quizMapper.toAuthoringDto(quiz);
    }

    @Transactional
    public QuizDraftDto upsertQuiz(UUID lessonId, QuizDraftDto dto) {
        CourseLesson lesson = requireLesson(lessonId);
        ensureOwner(lesson);
        ensureQuizLesson(lesson);
        quizValidator.validateDraft(dto);

        Quiz quiz = lesson.getQuiz();
        if (quiz != null && dto.getId() != null && !quiz.getId().toString().equals(dto.getId())) {
            throw new QuizValidationException("Quiz identifier mismatch", Map.of(
                    "id", "Existing quiz id does not match payload")
            );
        }
        if (quiz == null) {
            quiz = new Quiz();
        }
        quizMapper.applyDraft(quiz, dto, lesson);
        Quiz saved = quizRepository.save(quiz);
        lesson.setQuiz(saved);
        return quizMapper.toAuthoringDto(saved);
    }

    @Transactional
    public void deleteQuiz(UUID lessonId) {
        CourseLesson lesson = requireLesson(lessonId);
        ensureOwner(lesson);
        ensureQuizLesson(lesson);

        Quiz quiz = lesson.getQuiz();
        if (quiz == null) {
            throw new ResourceNotFoundException("Quiz not found for lesson " + lessonId);
        }
        lesson.setQuiz(null);
        quizRepository.delete(quiz);
    }

    private CourseLesson requireLesson(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
    }

    private void ensureOwner(CourseLesson lesson) {
        UUID currentUserId = currentUserService.getCurrentUserId();
        UUID ownerId = lesson.getSection().getCourse().getUser().getUserId();
        if (!ownerId.equals(currentUserId)) {
            throw new AccessDeniedException("You don’t own this lesson");
        }
    }

    private void ensureQuizLesson(CourseLesson lesson) {
        if (lesson.getType() != LessonType.QUIZ) {
            throw new QuizValidationException("Lesson does not support quizzes", Map.of(
                    "lessonId", "Lesson is not of type QUIZ")
            );
        }
    }
}

package com.myproject.video.video_platform.service.product.course.quiz;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.common.enums.products.course.LessonType;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizOptionDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizQuestionDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionAnswerDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionRequest;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionResponse;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import com.myproject.video.video_platform.entity.products.course.quiz.QuizAttempt;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.repository.products.course.quiz.QuizAttemptRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class LessonQuizIntegrationTest {

    @Autowired
    private LessonQuizAuthoringService authoringService;
    @Autowired
    private LessonQuizPlayerService playerService;
    @Autowired
    private CourseProductRepository courseProductRepository;
    @Autowired
    private CourseSectionRepository courseSectionRepository;
    @Autowired
    private CourseLessonRepository courseLessonRepository;
    @Autowired
    private QuizAttemptRepository attemptRepository;
    @Autowired
    private UserRepository userRepository;

    @MockBean
    private CurrentUserService currentUserService;

    private final AtomicReference<UUID> currentUser = new AtomicReference<>();

    @BeforeEach
    void setupMocks() {
        Mockito.when(currentUserService.getCurrentUserId()).thenAnswer(invocation -> {
            UUID userId = currentUser.get();
            if (userId == null) {
                throw new IllegalStateException("Current user not configured");
            }
            return userId;
        });
    }

    @Test
    void teacherCanAuthorQuizAndLearnerCanSubmit() {
        User owner = persistUser("owner@example.com");
        User learner = persistUser("learner@example.com");
        CourseLesson lesson = createLesson(owner);

        currentUser.set(owner.getUserId());
        QuizDraftDto created = authoringService.upsertQuiz(lesson.getId(), buildDraft());

        currentUser.set(learner.getUserId());
        QuizDraftDto playView = playerService.getQuizForPlay(lesson.getId());
        assertNull(playView.getQuestions().get(0).getOptions().get(0).getIsCorrect(), "Play mode must hide correctness");

        QuizSubmissionRequest submission = new QuizSubmissionRequest();
        QuizSubmissionAnswerDto answer = new QuizSubmissionAnswerDto();
        answer.setQuestionId(created.getQuestions().get(0).getId());
        answer.setSelectedOptionIds(List.of(created.getQuestions().get(0).getOptions().get(0).getId()));
        submission.setAnswers(List.of(answer));

        QuizSubmissionResponse response = playerService.submit(lesson.getId(), submission);
        assertTrue(response.isPassed());
        assertEquals(0, response.getPercentage().compareTo(BigDecimal.valueOf(100.0)));
        assertEquals(1, response.getQuestions().size());
        assertTrue(response.getQuestions().get(0).isCorrect());

        List<QuizAttempt> attempts = attemptRepository.findAll();
        assertEquals(1, attempts.size());
        QuizAttempt attempt = attempts.get(0);
        assertEquals(lesson.getId(), attempt.getLesson().getId());
        assertEquals(learner.getUserId(), attempt.getUser().getUserId());
        assertTrue(attempt.isPassed());
        assertFalse(attempt.getAnswersJson().isBlank());
        assertNotNull(attempt.getQuiz());
    }

    private QuizDraftDto buildDraft() {
        QuizDraftDto dto = new QuizDraftDto();
        dto.setTitle("Quiz");
        dto.setPassingScore(60);
        QuizQuestionDto question = new QuizQuestionDto();
        question.setTitle("Question 1");
        question.setType("multiple_choice_single");
        question.setPoints(5);
        QuizOptionDto correct = new QuizOptionDto();
        correct.setText("Correct");
        correct.setIsCorrect(true);
        QuizOptionDto wrong = new QuizOptionDto();
        wrong.setText("Wrong");
        wrong.setIsCorrect(false);
        question.setOptions(List.of(correct, wrong));
        dto.setQuestions(List.of(question));
        return dto;
    }

    private CourseLesson createLesson(User owner) {
        CourseProduct course = new CourseProduct();
        course.setName("Course");
        course.setDescription("description");
        course.setStatus(ProductStatus.PUBLISHED);
        course.setType(ProductType.COURSE);
        course.setUser(owner);
        course = courseProductRepository.save(course);

        CourseSection section = new CourseSection();
        section.setTitle("Section");
        section.setPosition(1);
        section.setCourse(course);
        section = courseSectionRepository.save(section);

        CourseLesson lesson = new CourseLesson();
        lesson.setTitle("Quiz lesson");
        lesson.setType(LessonType.QUIZ);
        lesson.setSection(section);
        lesson.setPosition(1);
        return courseLessonRepository.save(lesson);
    }

    private User persistUser(String email) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEnabled(true);
        return userRepository.save(user);
    }
}

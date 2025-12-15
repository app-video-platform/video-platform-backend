package com.myproject.video.video_platform.controller.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.common.enums.products.course.LessonType;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizDraftDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizOptionDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizQuestionDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionAnswerDto;
import com.myproject.video.video_platform.dto.products.course.quiz.QuizSubmissionRequest;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LessonQuizControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseProductRepository courseProductRepository;
    @Autowired
    private CourseSectionRepository courseSectionRepository;
    @Autowired
    private CourseLessonRepository courseLessonRepository;
    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @MockBean
    private CurrentUserService currentUserService;

    private final AtomicReference<UUID> currentUser = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        Mockito.when(currentUserService.getCurrentUserId()).thenAnswer(invocation -> {
            UUID userId = currentUser.get();
            if (userId == null) {
                throw new IllegalStateException("Current user not configured");
            }
            return userId;
        });

        quizAttemptRepository.deleteAll();
        courseLessonRepository.deleteAll();
        courseSectionRepository.deleteAll();
        courseProductRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void ownerCanUpsertAndFetchQuizForAuthoring() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());
        CourseLesson lesson = createLesson(owner, LessonType.QUIZ, ProductStatus.DRAFT);

        QuizDraftDto draft = buildDraft();

        mockMvc.perform(put("/api/lessons/{lessonId}/quiz", lesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].options[0].isCorrect").value(true));

        mockMvc.perform(get("/api/lessons/{lessonId}/quiz", lesson.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].options[0].isCorrect").value(true));
    }

    @Test
    void learnerCanPlayAndSubmitQuiz() throws Exception {
        User owner = persistUser("owner@example.com");
        User learner = persistUser("learner@example.com");
        currentUser.set(owner.getUserId());
        CourseLesson lesson = createLesson(owner, LessonType.QUIZ, ProductStatus.PUBLISHED);

        QuizDraftDto draft = buildDraft();
        MvcResult upserted = mockMvc.perform(put("/api/lessons/{lessonId}/quiz", lesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draft)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode quizJson = objectMapper.readTree(upserted.getResponse().getContentAsString());
        String questionId = quizJson.at("/questions/0/id").asText();
        String correctOptionId = quizJson.at("/questions/0/options/0/id").asText();

        currentUser.set(learner.getUserId());
        mockMvc.perform(get("/api/lessons/{lessonId}/quiz/play", lesson.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].options[0].isCorrect").doesNotExist());

        QuizSubmissionAnswerDto answer = new QuizSubmissionAnswerDto();
        answer.setQuestionId(questionId);
        answer.setSelectedOptionIds(List.of(correctOptionId));
        QuizSubmissionRequest request = new QuizSubmissionRequest();
        request.setAnswers(List.of(answer));

        mockMvc.perform(post("/api/lessons/{lessonId}/quiz/submit", lesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passed").value(true))
                .andExpect(jsonPath("$.percentage").value(100.0));

        assertEquals(1, quizAttemptRepository.count());
    }

    @Test
    void upsertQuiz_onNonQuizLesson_returnsBadRequest() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());
        CourseLesson lesson = createLesson(owner, LessonType.VIDEO, ProductStatus.DRAFT);

        mockMvc.perform(put("/api/lessons/{lessonId}/quiz", lesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDraft())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteQuiz_removesQuizDefinition() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());
        CourseLesson lesson = createLesson(owner, LessonType.QUIZ, ProductStatus.DRAFT);

        mockMvc.perform(put("/api/lessons/{lessonId}/quiz", lesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildDraft())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/lessons/{lessonId}/quiz", lesson.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/lessons/{lessonId}/quiz", lesson.getId()))
                .andExpect(status().isNotFound());
    }

    private QuizDraftDto buildDraft() {
        QuizDraftDto dto = new QuizDraftDto();
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

    private CourseLesson createLesson(User owner, LessonType lessonType, ProductStatus status) {
        CourseProduct course = new CourseProduct();
        course.setName("Course");
        course.setDescription("description");
        course.setStatus(status);
        course.setType(ProductType.COURSE);
        course.setUser(owner);
        course.setPrice(BigDecimal.ZERO);
        course = courseProductRepository.save(course);

        CourseSection section = new CourseSection();
        section.setTitle("Section");
        section.setPosition(1);
        section.setCourse(course);
        section = courseSectionRepository.save(section);

        CourseLesson lesson = new CourseLesson();
        lesson.setTitle("Lesson");
        lesson.setType(lessonType);
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

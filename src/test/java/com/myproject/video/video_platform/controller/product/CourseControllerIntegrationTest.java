package com.myproject.video.video_platform.controller.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.video.video_platform.dto.products.course.CourseLessonCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseLessonUpdateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionCreateRequestDto;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CourseControllerIntegrationTest {

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

        courseLessonRepository.deleteAll();
        courseSectionRepository.deleteAll();
        courseProductRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createSection_nonOwner_returnsBadRequest() throws Exception {
        User owner = persistUser("owner@example.com");
        User other = persistUser("other@example.com");

        currentUser.set(owner.getUserId());
        UUID courseId = createCourseProduct(owner);

        currentUser.set(other.getUserId());
        CourseSectionCreateRequestDto dto = new CourseSectionCreateRequestDto();
        dto.setTitle("Section");
        dto.setProductId(courseId.toString());
        dto.setUserId(owner.getUserId().toString());
        dto.setPosition(2);

        mockMvc.perform(post("/api/products/course/section")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAndUpdateLesson_switchingType_clearsAndSetsFields() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        UUID courseId = createCourseProduct(owner);
        UUID draftSectionId = getDraftSectionId(courseId);

        CourseLessonCreateRequestDto create = new CourseLessonCreateRequestDto();
        create.setTitle("Video lesson");
        create.setType("VIDEO");
        create.setSectionId(draftSectionId.toString());
        create.setUserId(owner.getUserId().toString());
        create.setVideoUrl("https://cdn.example.com/video.mp4");
        create.setContent("<p>ignored</p>");

        MvcResult created = mockMvc.perform(post("/api/products/course/section/lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        UUID lessonId = UUID.fromString(createdJson.get("id").asText());
        assertEquals("VIDEO", createdJson.get("type").asText());
        assertEquals("https://cdn.example.com/video.mp4", createdJson.get("videoUrl").asText());
        assertTrue(createdJson.path("content").isMissingNode() || createdJson.path("content").isNull());

        CourseLesson lessonEntity = courseLessonRepository.findById(lessonId).orElseThrow();
        assertNotNull(lessonEntity.getVideoUrl());
        assertNull(lessonEntity.getContent());

        CourseLessonUpdateRequestDto update = new CourseLessonUpdateRequestDto();
        update.setId(lessonId.toString());
        update.setUserId(owner.getUserId().toString());
        update.setTitle("Article lesson");
        update.setType("ARTICLE");
        update.setContent("<p>Hello</p>");
        update.setVideoUrl("https://should.be.cleared");

        mockMvc.perform(put("/api/products/course/section/lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        CourseLesson updatedEntity = courseLessonRepository.findById(lessonId).orElseThrow();
        assertEquals("Article lesson", updatedEntity.getTitle());
        assertEquals("ARTICLE", updatedEntity.getType().name());
        assertEquals("<p>Hello</p>", updatedEntity.getContent());
        assertNull(updatedEntity.getVideoUrl());
    }

    @Test
    void deleteSection_cascadesLessons() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        UUID courseId = createCourseProduct(owner);
        UUID draftSectionId = getDraftSectionId(courseId);

        CourseLessonCreateRequestDto create = new CourseLessonCreateRequestDto();
        create.setTitle("Video lesson");
        create.setType("VIDEO");
        create.setSectionId(draftSectionId.toString());
        create.setUserId(owner.getUserId().toString());
        create.setVideoUrl("https://cdn.example.com/video.mp4");

        mockMvc.perform(post("/api/products/course/section/lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated());

        assertEquals(1, courseLessonRepository.count());

        mockMvc.perform(delete("/api/products/course/section")
                        .param("userId", owner.getUserId().toString())
                        .param("id", draftSectionId.toString()))
                .andExpect(status().isOk());

        assertEquals(0, courseLessonRepository.count());
        assertEquals(0, courseSectionRepository.count());
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

    private UUID createCourseProduct(User owner) throws Exception {
        CourseProductRequestDto dto = new CourseProductRequestDto();
        dto.setType("COURSE");
        dto.setName("Course");
        dto.setDescription("Desc");
        dto.setStatus("DRAFT");
        dto.setPrice("free");
        dto.setUserId(owner.getUserId().toString());

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(json.get("id").asText());
    }

    private UUID getDraftSectionId(UUID courseId) {
        return courseSectionRepository.findAll().stream()
                .filter(section -> section.getCourse().getId().equals(courseId))
                .findFirst()
                .map(section -> section.getId())
                .orElseThrow();
    }
}

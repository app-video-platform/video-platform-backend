package com.myproject.video.video_platform.controller.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.video.video_platform.dto.products.course.CourseLessonCreateRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.course.CourseSectionCreateRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductDetailsRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductRequestDto;
import com.myproject.video.video_platform.dto.products.download.SectionDownloadProductRequestDto;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductDetailsDto;
import com.myproject.video.video_platform.dto.products.consultation.ConsultationProductRequestDto;
import com.myproject.video.video_platform.entity.products.consultation.ConnectedCalendar;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.consultation.ConnectedCalendarRepository;
import com.myproject.video.video_platform.repository.products.consultation.ConsultationProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.repository.products.download.DownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download.FileDownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download.SectionDownloadProductRepository;
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

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

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
    private DownloadProductRepository downloadProductRepository;
    @Autowired
    private SectionDownloadProductRepository sectionDownloadProductRepository;
    @Autowired
    private FileDownloadProductRepository fileDownloadProductRepository;
    @Autowired
    private ConsultationProductRepository consultationProductRepository;
    @Autowired
    private ConnectedCalendarRepository connectedCalendarRepository;

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

        fileDownloadProductRepository.deleteAll();
        sectionDownloadProductRepository.deleteAll();
        downloadProductRepository.deleteAll();

        courseLessonRepository.deleteAll();
        courseSectionRepository.deleteAll();
        courseProductRepository.deleteAll();

        consultationProductRepository.deleteAll();
        connectedCalendarRepository.deleteAll();

        userRepository.deleteAll();
    }

    @Test
    void createCourseProduct_returnsDraftSectionInDetails() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        CourseProductRequestDto dto = new CourseProductRequestDto();
        dto.setType("COURSE");
        dto.setName("Course");
        dto.setDescription("Desc");
        dto.setStatus(null);
        dto.setPrice("free");
        dto.setUserId(owner.getUserId().toString());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("COURSE"))
                .andExpect(jsonPath("$.details.sections", hasSize(1)))
                .andExpect(jsonPath("$.details.sections[0].title").value("Draft"));
    }

    @Test
    void createDownloadProduct_withSections_persistsAndReturnsDetails() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        SectionDownloadProductRequestDto section = new SectionDownloadProductRequestDto();
        section.setTitle("Files");
        section.setDescription("Desc");
        section.setPosition(1);
        DownloadProductDetailsRequestDto details = new DownloadProductDetailsRequestDto();
        details.setSections(List.of(section));

        DownloadProductRequestDto dto = new DownloadProductRequestDto();
        dto.setType("DOWNLOAD");
        dto.setName("Download");
        dto.setDescription("Desc");
        dto.setStatus("DRAFT");
        dto.setPrice("0");
        dto.setUserId(owner.getUserId().toString());
        dto.setDetails(details);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DOWNLOAD"))
                .andExpect(jsonPath("$.details.sections", hasSize(1)))
                .andExpect(jsonPath("$.details.sections[0].title").value("Files"));
    }

    @Test
    void getAllProductsMin_withoutUserId_returnsAllProducts() throws Exception {
        User owner = persistUser("owner@example.com");
        User other = persistUser("other@example.com");

        currentUser.set(owner.getUserId());
        createCourseProduct(owner);

        currentUser.set(other.getUserId());
        createDownloadProduct(other, "Files");

        mockMvc.perform(get("/api/products/get-all-products-min"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Course", "Download")));
    }

    @Test
    void getAllProductsMin_withUserId_returnsOnlyThatUsersProducts() throws Exception {
        User owner = persistUser("owner@example.com");
        User other = persistUser("other@example.com");

        currentUser.set(owner.getUserId());
        createCourseProduct(owner);

        currentUser.set(other.getUserId());
        createDownloadProduct(other, "Files");

        mockMvc.perform(get("/api/products/get-all-products-min")
                        .param("userId", owner.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Course")));
    }

    @Test
    void exploreSearch_findsByProductName() throws Exception {
        User owner = persistUser("owner@example.com");
        User other = persistUser("other@example.com");

        currentUser.set(owner.getUserId());
        CourseProductRequestDto course = new CourseProductRequestDto();
        course.setType("COURSE");
        course.setName("Photography Masterclass");
        course.setDescription("Desc");
        course.setStatus("DRAFT");
        course.setPrice("free");
        course.setUserId(owner.getUserId().toString());
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isCreated());

        currentUser.set(other.getUserId());
        CourseProductRequestDto otherCourse = new CourseProductRequestDto();
        otherCourse.setType("COURSE");
        otherCourse.setName("Cooking 101");
        otherCourse.setDescription("Desc");
        otherCourse.setStatus("DRAFT");
        otherCourse.setPrice("free");
        otherCourse.setUserId(other.getUserId().toString());
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherCourse)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products/search")
                        .param("term", "photo")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Photography Masterclass"));
    }

    @Test
    void librarySearch_findsOnlyProductsOwnedByUser() throws Exception {
        User owner = persistUser("owner@example.com");
        User other = persistUser("other@example.com");

        currentUser.set(owner.getUserId());
        CourseProductRequestDto course = new CourseProductRequestDto();
        course.setType("COURSE");
        course.setName("Photography Masterclass");
        course.setDescription("Desc");
        course.setStatus("DRAFT");
        course.setPrice("free");
        course.setUserId(owner.getUserId().toString());
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isCreated());

        currentUser.set(other.getUserId());
        CourseProductRequestDto otherCourse = new CourseProductRequestDto();
        otherCourse.setType("COURSE");
        otherCourse.setName("Photography for Beginners");
        otherCourse.setDescription("Desc");
        otherCourse.setStatus("DRAFT");
        otherCourse.setPrice("free");
        otherCourse.setUserId(other.getUserId().toString());
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherCourse)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products/search/{userId}/products", owner.getUserId().toString())
                        .param("term", "photo")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].createdById").value(owner.getUserId().toString()))
                .andExpect(jsonPath("$.content[0].title").value("Photography Masterclass"));
    }

    @Test
    void updateDownloadProduct_withoutDetails_doesNotWipeSections() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        UUID downloadId = createDownloadProduct(owner, "Files");

        JsonNode before = getProduct(downloadId, "DOWNLOAD");
        String sectionId = before.at("/details/sections/0/id").asText();

        DownloadProductRequestDto update = new DownloadProductRequestDto();
        update.setId(downloadId.toString());
        update.setType("DOWNLOAD");
        update.setName("Download updated");
        update.setUserId(owner.getUserId().toString());
        update.setDetails(null);

        mockMvc.perform(put("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.details.sections", hasSize(1)))
                .andExpect(jsonPath("$.details.sections[0].id").value(sectionId));
    }

    @Test
    void updateDownloadProduct_withEmptySections_clearsSections() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        UUID downloadId = createDownloadProduct(owner, "Files");

        DownloadProductDetailsRequestDto details = new DownloadProductDetailsRequestDto();
        details.setSections(List.of());
        DownloadProductRequestDto update = new DownloadProductRequestDto();
        update.setId(downloadId.toString());
        update.setType("DOWNLOAD");
        update.setUserId(owner.getUserId().toString());
        update.setDetails(details);

        mockMvc.perform(put("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.details.sections", hasSize(0)));

        JsonNode after = getProduct(downloadId, "DOWNLOAD");
        assertEquals(0, after.at("/details/sections").size());
    }

    @Test
    void deleteProduct_removesEntity() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        UUID courseId = createCourseProduct(owner);

        mockMvc.perform(delete("/api/products")
                        .param("userId", owner.getUserId().toString())
                        .param("productType", "COURSE")
                        .param("id", courseId.toString()))
                .andExpect(status().isAccepted());

        mockMvc.perform(get("/api/products/getProduct")
                        .param("productId", courseId.toString())
                        .param("type", "COURSE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProduct_invalidTypeParam_returnsBadRequest() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());
        UUID courseId = createCourseProduct(owner);

        mockMvc.perform(get("/api/products/getProduct")
                        .param("productId", courseId.toString())
                        .param("type", "NOT_A_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_userNotFound_returnsNotFound() throws Exception {
        UUID missingUserId = UUID.randomUUID();
        currentUser.set(missingUserId);

        CourseProductRequestDto dto = new CourseProductRequestDto();
        dto.setType("COURSE");
        dto.setName("Course");
        dto.setDescription("Desc");
        dto.setStatus("DRAFT");
        dto.setPrice("free");
        dto.setUserId(missingUserId.toString());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateConsultationProduct_withoutDetails_doesNotWipeExistingDetails() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        ConsultationProductDetailsDto details = new ConsultationProductDetailsDto();
        details.setDurationMinutes(60);
        details.setMeetingMethod(ConsultationProductDetailsDto.MeetingMethod.ZOOM);
        details.setBufferBeforeMinutes(5);
        details.setBufferAfterMinutes(10);
        details.setCancellationPolicy("No refunds");

        ConsultationProductRequestDto create = new ConsultationProductRequestDto();
        create.setType("CONSULTATION");
        create.setName("Consult");
        create.setDescription("Desc");
        create.setStatus("DRAFT");
        create.setPrice("free");
        create.setUserId(owner.getUserId().toString());
        create.setDetails(details);

        MvcResult created = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        UUID id = UUID.fromString(createdJson.get("id").asText());

        ConsultationProductRequestDto update = new ConsultationProductRequestDto();
        update.setId(id.toString());
        update.setType("CONSULTATION");
        update.setName("Consult updated");
        update.setUserId(owner.getUserId().toString());
        update.setDetails(null);

        mockMvc.perform(put("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.name").value("Consult updated"))
                .andExpect(jsonPath("$.details.durationMinutes").value(60))
                .andExpect(jsonPath("$.details.meetingMethod").value("ZOOM"));
    }

    @Test
    void getAllProductsForUser_returnsFullDetails_includingNewSectionsAndLessons() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        UUID courseId = createCourseProduct(owner);
        UUID sectionId = createCourseSection(owner, courseId, "Module 1", 2);
        createCourseLesson(owner, sectionId, "Intro", "VIDEO");

        MvcResult result = mockMvc.perform(get("/api/products")
                        .param("userId", owner.getUserId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, root.size());
        JsonNode product = root.get(0);
        assertEquals("COURSE", product.get("type").asText());
        JsonNode sections = product.at("/details/sections");
        assertTrue(sections.isArray());
        JsonNode module1 = null;
        for (JsonNode section : sections) {
            if ("Module 1".equals(section.get("title").asText())) {
                module1 = section;
                break;
            }
        }
        assertNotNull(module1, "Expected Module 1 section in course details");
        assertEquals("Intro", module1.at("/lessons/0/title").asText());
    }

    @Test
    void consultationProductResponse_includesConnectedCalendarsUnderDetails() throws Exception {
        User owner = persistUser("owner@example.com");
        currentUser.set(owner.getUserId());

        UUID consultationId = createConsultationProduct(owner);

        ConnectedCalendar cal = ConnectedCalendar.builder()
                .teacherId(owner.getUserId())
                .provider(ConnectedCalendar.Provider.GOOGLE)
                .oauthTokenEnc("enc")
                .refreshTokenEnc("enc2")
                .expiresAt(ZonedDateTime.now())
                .build();
        connectedCalendarRepository.save(cal);

        mockMvc.perform(get("/api/products/getProduct")
                        .param("productId", consultationId.toString())
                        .param("type", "CONSULTATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("CONSULTATION"))
                .andExpect(jsonPath("$.details.connectedCalendars", hasSize(1)))
                .andExpect(jsonPath("$.details.connectedCalendars[0].provider").value("GOOGLE"));
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

    private UUID createDownloadProduct(User owner, String sectionTitle) throws Exception {
        SectionDownloadProductRequestDto section = new SectionDownloadProductRequestDto();
        section.setTitle(sectionTitle);
        section.setPosition(1);
        DownloadProductDetailsRequestDto details = new DownloadProductDetailsRequestDto();
        details.setSections(List.of(section));

        DownloadProductRequestDto dto = new DownloadProductRequestDto();
        dto.setType("DOWNLOAD");
        dto.setName("Download");
        dto.setDescription("Desc");
        dto.setStatus("DRAFT");
        dto.setPrice("free");
        dto.setUserId(owner.getUserId().toString());
        dto.setDetails(details);

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(json.get("id").asText());
    }

    private UUID createConsultationProduct(User owner) throws Exception {
        ConsultationProductRequestDto dto = new ConsultationProductRequestDto();
        dto.setType("CONSULTATION");
        dto.setName("Consult");
        dto.setDescription("Desc");
        dto.setStatus("DRAFT");
        dto.setPrice("free");
        dto.setUserId(owner.getUserId().toString());
        dto.setDetails(null);

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(json.get("id").asText());
    }

    private JsonNode getProduct(UUID productId, String type) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products/getProduct")
                        .param("productId", productId.toString())
                        .param("type", type))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private UUID createCourseSection(User owner, UUID courseId, String title, Integer position) throws Exception {
        CourseSectionCreateRequestDto dto = new CourseSectionCreateRequestDto();
        dto.setTitle(title);
        dto.setProductId(courseId.toString());
        dto.setUserId(owner.getUserId().toString());
        dto.setDescription("");
        dto.setPosition(position);

        MvcResult result = mockMvc.perform(post("/api/products/course/section")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return UUID.fromString(json.get("id").asText());
    }

    private void createCourseLesson(User owner, UUID sectionId, String title, String type) throws Exception {
        CourseLessonCreateRequestDto dto = new CourseLessonCreateRequestDto();
        dto.setTitle(title);
        dto.setType(type);
        dto.setSectionId(sectionId.toString());
        dto.setUserId(owner.getUserId().toString());
        dto.setVideoUrl("https://cdn.example.com/video.mp4");
        dto.setContent("<p>ignored</p>");

        mockMvc.perform(post("/api/products/course/section/lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}

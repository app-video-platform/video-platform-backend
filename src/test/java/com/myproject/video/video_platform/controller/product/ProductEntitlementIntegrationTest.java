package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.common.enums.products.course.LessonType;
import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.entity.products.course.CourseLesson;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.course.CourseSection;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.entitlement.ProductEntitlementRepository;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.service.security.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductEntitlementIntegrationTest {

    private static final String CSRF = "test-csrf-token";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CourseProductRepository productRepository;
    @Autowired
    private CourseSectionRepository sectionRepository;
    @Autowired
    private CourseLessonRepository lessonRepository;
    @Autowired
    private ProductEntitlementRepository entitlementRepository;

    private Role userRole;
    private User learner;

    @BeforeEach
    void setUp() {
        entitlementRepository.deleteAll();
        lessonRepository.deleteAll();
        sectionRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRole = roleRepository.save(new Role(null, UserRole.USER.name()));
        roleRepository.save(new Role(null, UserRole.CREATOR.name()));
        learner = persistUser("learner@example.com");
    }

    @AfterEach
    void tearDown() {
        entitlementRepository.deleteAll();
    }

    @Test
    void freePublishedProductCanBeEnrolledAndAppearsInLibrary() throws Exception {
        CourseProduct product = createCourse(BigDecimal.ZERO, ProductStatus.PUBLISHED);

        mockMvc.perform(post("/api/entitlements/products/{productId}/enroll", product.getId())
                        .cookie(authCookie(learner), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("FREE_ENROLLMENT"))
                .andExpect(jsonPath("$.product.id").value(product.getId().toString()));

        mockMvc.perform(get("/api/entitlements/me").cookie(authCookie(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product.id").value(product.getId().toString()));

        mockMvc.perform(get("/api/entitlements/products/{productId}/access", product.getId())
                        .cookie(authCookie(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasAccess").value(true));
    }

    @Test
    void paidProductRequiresPaymentAndDoesNotCreateEntitlement() throws Exception {
        CourseProduct product = createCourse(new BigDecimal("19.99"), ProductStatus.PUBLISHED);

        mockMvc.perform(post("/api/entitlements/products/{productId}/enroll", product.getId())
                        .cookie(authCookie(learner), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF))
                .andExpect(status().isPaymentRequired());

        mockMvc.perform(get("/api/entitlements/me").cookie(authCookie(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void publicCourseResponseShowsOutlineButNotLessonBodyUntilEnrolled() throws Exception {
        CourseProduct product = createCourse(BigDecimal.ZERO, ProductStatus.PUBLISHED);
        createArticleLesson(product, "<p>Protected lesson</p>");

        mockMvc.perform(get("/api/products/{productId}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.sections[0].lessons[0].title").value("Article"))
                .andExpect(jsonPath("$.details.sections[0].lessons[0].content").doesNotExist());

        enroll(product);

        mockMvc.perform(get("/api/products/{productId}", product.getId())
                        .cookie(authCookie(learner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details.sections[0].lessons[0].content")
                        .value("<p>Protected lesson</p>"));
    }

    @Test
    void draftProductIsNotPubliclyReadable() throws Exception {
        CourseProduct product = createCourse(BigDecimal.ZERO, ProductStatus.DRAFT);

        mockMvc.perform(get("/api/products/{productId}", product.getId()))
                .andExpect(status().isForbidden());
    }

    private void enroll(CourseProduct product) throws Exception {
        mockMvc.perform(post("/api/entitlements/products/{productId}/enroll", product.getId())
                        .cookie(authCookie(learner), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF))
                .andExpect(status().isCreated());
    }

    private CourseProduct createCourse(BigDecimal price, ProductStatus status) {
        User creator = persistUser("creator-" + UUID.randomUUID() + "@example.com");
        CourseProduct product = new CourseProduct();
        product.setName("Course");
        product.setDescription("Description");
        product.setStatus(status);
        product.setType(ProductType.COURSE);
        product.setPrice(price);
        product.setUser(creator);
        return productRepository.save(product);
    }

    private void createArticleLesson(CourseProduct product, String content) {
        CourseSection section = new CourseSection();
        section.setTitle("Section");
        section.setDescription("Outline");
        section.setPosition(1);
        section.setCourse(product);
        section = sectionRepository.save(section);

        CourseLesson lesson = new CourseLesson();
        lesson.setTitle("Article");
        lesson.setDescription("Summary");
        lesson.setType(LessonType.ARTICLE);
        lesson.setContent(content);
        lesson.setPosition(1);
        lesson.setSection(section);
        lessonRepository.save(lesson);
    }

    private User persistUser(String email) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEnabled(true);
        user.setRoles(Set.of(userRole));
        return userRepository.save(user);
    }

    private Cookie authCookie(User user) {
        return new Cookie("JWT_TOKEN", jwtProvider.generateToken(user));
    }

    private Cookie csrfCookie() {
        return new Cookie("XSRF-TOKEN", CSRF);
    }
}

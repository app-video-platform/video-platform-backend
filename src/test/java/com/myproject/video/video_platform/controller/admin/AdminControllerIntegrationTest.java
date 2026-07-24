package com.myproject.video.video_platform.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.admin.AdminRoleUpdateRequest;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.entity.auth.RefreshToken;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.admin.AdminAuditLogRepository;
import com.myproject.video.video_platform.repository.auth.RefreshTokenRepository;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.ProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.service.security.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTest {

    private static final String CSRF = "test-csrf-token";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CourseProductRepository courseProductRepository;
    @Autowired
    private CourseSectionRepository courseSectionRepository;
    @Autowired
    private CourseLessonRepository courseLessonRepository;
    @Autowired
    private AdminAuditLogRepository auditLogRepository;

    private Role adminRole;
    private Role creatorRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        courseLessonRepository.deleteAll();
        courseSectionRepository.deleteAll();
        courseProductRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        adminRole = roleRepository.save(new Role(null, UserRole.ADMIN.name()));
        creatorRole = roleRepository.save(new Role(null, UserRole.CREATOR.name()));
        userRole = roleRepository.save(new Role(null, UserRole.USER.name()));
    }

    @Test
    void adminCanListUsersButCreatorCannot() throws Exception {
        User admin = persistUser("admin@example.com", adminRole);
        User creator = persistUser("creator@example.com", creatorRole);
        persistUser("normal@example.com", userRole);

        mockMvc.perform(get("/api/admin/users")
                        .cookie(authCookie(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));

        mockMvc.perform(get("/api/admin/users")
                        .cookie(authCookie(creator)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoleUpdateReplacesRoleDeletesRefreshTokensAndAudits() throws Exception {
        User admin = persistUser("admin@example.com", adminRole);
        User target = persistUser("target@example.com", userRole);
        persistRefreshToken(target);

        AdminRoleUpdateRequest request = new AdminRoleUpdateRequest();
        request.setRole(UserRole.CREATOR);

        mockMvc.perform(patch("/api/admin/users/{userId}/role", target.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(admin), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem(UserRole.CREATOR.name())));

        User updated = userRepository.findById(target.getUserId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(1, updated.getRoles().size());
        org.junit.jupiter.api.Assertions.assertTrue(updated.getRoles().stream()
                .anyMatch(role -> UserRole.CREATOR.name().equals(role.getRoleName())));
        org.junit.jupiter.api.Assertions.assertTrue(refreshTokenRepository.findAll().isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals(1, auditLogRepository.findAll().size());
    }

    @Test
    void cannotRemoveLastAdminRole() throws Exception {
        User admin = persistUser("admin@example.com", adminRole);

        AdminRoleUpdateRequest request = new AdminRoleUpdateRequest();
        request.setRole(UserRole.USER);

        mockMvc.perform(patch("/api/admin/users/{userId}/role", admin.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(admin), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateForCreatorButNotUserAndCreatorSpoofIsIgnored() throws Exception {
        User admin = persistUser("admin@example.com", adminRole);
        User creator = persistUser("creator@example.com", creatorRole);
        User otherCreator = persistUser("other-creator@example.com", creatorRole);
        User normalUser = persistUser("normal@example.com", userRole);

        CourseProductRequestDto adminCreate = courseRequest();
        adminCreate.setUserId(creator.getUserId().toString());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(admin), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(adminCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(creator.getUserId().toString()));

        CourseProductRequestDto invalidOwner = courseRequest();
        invalidOwner.setUserId(normalUser.getUserId().toString());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(admin), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(invalidOwner)))
                .andExpect(status().isForbidden());

        CourseProductRequestDto spoofed = courseRequest();
        spoofed.setUserId(otherCreator.getUserId().toString());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(creator), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(spoofed)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(creator.getUserId().toString()));
    }

    @Test
    void adminCanEditDeleteAnyProductAndCreatorCannotEditOthersProduct() throws Exception {
        User admin = persistUser("admin@example.com", adminRole);
        User owner = persistUser("owner@example.com", creatorRole);
        User otherCreator = persistUser("other@example.com", creatorRole);
        UUID productId = createCourse(owner);

        CourseProductRequestDto patchBody = courseRequest();
        patchBody.setName("Renamed");

        mockMvc.perform(patch("/api/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(otherCreator), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(patchBody)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(admin), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(patchBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"));

        mockMvc.perform(delete("/api/products/{productId}", productId)
                        .cookie(authCookie(admin), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(productRepository.existsById(productId));
        org.junit.jupiter.api.Assertions.assertTrue(auditLogRepository.findAll().stream()
                .anyMatch(log -> "PRODUCT_UPDATE".equals(log.getAction())));
        org.junit.jupiter.api.Assertions.assertTrue(auditLogRepository.findAll().stream()
                .anyMatch(log -> "PRODUCT_DELETE".equals(log.getAction())));
    }

    @Test
    void adminProductsAndAuditEndpointsReturnData() throws Exception {
        User admin = persistUser("admin@example.com", adminRole);
        User creator = persistUser("creator@example.com", creatorRole);
        createCourse(creator);

        mockMvc.perform(get("/api/admin/products")
                        .param("ownerId", creator.getUserId().toString())
                        .cookie(authCookie(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/admin/audit")
                        .cookie(authCookie(admin)))
                .andExpect(status().isOk());
    }

    private UUID createCourse(User owner) throws Exception {
        CourseProductRequestDto dto = courseRequest();
        dto.setUserId(owner.getUserId().toString());

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(owner), csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asText();
        return UUID.fromString(id);
    }

    private CourseProductRequestDto courseRequest() {
        CourseProductRequestDto dto = new CourseProductRequestDto();
        dto.setType("COURSE");
        dto.setName("Course");
        dto.setDescription("Desc");
        dto.setStatus("DRAFT");
        dto.setPrice("free");
        return dto;
    }

    private User persistUser(String email, Role role) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEnabled(true);
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }

    private void persistRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-" + user.getUserId());
        refreshToken.setUserEmail(user.getEmail());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
        refreshTokenRepository.save(refreshToken);
    }

    private Cookie authCookie(User user) {
        return new Cookie("JWT_TOKEN", jwtProvider.generateToken(user));
    }

    private Cookie csrfCookie() {
        return new Cookie("XSRF-TOKEN", CSRF);
    }
}

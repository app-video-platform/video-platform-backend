package com.myproject.video.video_platform.controller.smoke;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.authetication.GoogleLoginRequest;
import com.myproject.video.video_platform.dto.authetication.LoginRequest;
import com.myproject.video.video_platform.dto.authetication.RegisterRequest;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.entity.auth.VerificationToken;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.RefreshTokenRepository;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.auth.VerificationTokenRepository;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.repository.products.download.DownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download.FileDownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download.SectionDownloadProductRepository;
import com.myproject.video.video_platform.service.mail.EmailService;
import com.myproject.video.video_platform.service.security.GoogleTokenVerifier;
import jakarta.servlet.http.Cookie;
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

import java.util.Set;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthRoleSmokeIntegrationTest {

    private static final String CSRF = "smoke-csrf-token";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private CourseLessonRepository courseLessonRepository;
    @Autowired
    private CourseSectionRepository courseSectionRepository;
    @Autowired
    private CourseProductRepository courseProductRepository;
    @Autowired
    private DownloadProductRepository downloadProductRepository;
    @Autowired
    private SectionDownloadProductRepository sectionDownloadProductRepository;
    @Autowired
    private FileDownloadProductRepository fileDownloadProductRepository;

    @MockBean
    private EmailService emailService;
    @MockBean
    private GoogleTokenVerifier googleTokenVerifier;

    private Role userRole;
    private Role creatorRole;

    @BeforeEach
    void setUp() {
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        fileDownloadProductRepository.deleteAll();
        sectionDownloadProductRepository.deleteAll();
        downloadProductRepository.deleteAll();
        courseLessonRepository.deleteAll();
        courseSectionRepository.deleteAll();
        courseProductRepository.deleteAll();
        verificationTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRole = roleRepository.save(new Role(null, UserRole.USER.name()));
        creatorRole = roleRepository.save(new Role(null, UserRole.CREATOR.name()));
        roleRepository.save(new Role(null, UserRole.ADMIN.name()));
    }

    @Test
    void manualRegisterVerifyLoginProfileRefreshAndLogoutSmokeFlow() throws Exception {
        RegisterRequest register = registerRequest("manual-smoke@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        User registeredUser = userRepository.findByEmail(register.getEmail()).orElseThrow();
        assertFalse(registeredUser.isEnabled());
        assertTrue(registeredUser.getRoles().stream()
                .anyMatch(role -> UserRole.USER.name().equals(role.getRoleName())));

        VerificationToken verificationToken = verificationTokenRepository.findAll().stream()
                .filter(token -> token.getUser().getUserId().equals(registeredUser.getUserId()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/auth/verify")
                        .param("token", verificationToken.getToken()))
                .andExpect(status().isOk());

        assertTrue(userRepository.findByEmail(register.getEmail()).orElseThrow().isEnabled());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest(register.getEmail(), register.getPassword()))))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JWT_TOKEN"))
                .andExpect(cookie().exists("REFRESH_TOKEN"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        Cookie jwtCookie = responseCookie(loginResult, "JWT_TOKEN");
        Cookie refreshCookie = responseCookie(loginResult, "REFRESH_TOKEN");

        mockMvc.perform(get("/api/user/userInfo")
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(register.getEmail()))
                .andExpect(jsonPath("$.roles", hasItem(UserRole.USER.name())));

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JWT_TOKEN"))
                .andExpect(cookie().exists("REFRESH_TOKEN"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        mockMvc.perform(get("/api/user/userInfo")
                        .cookie(responseCookie(refreshResult, "JWT_TOKEN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem(UserRole.USER.name())));

        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("JWT_TOKEN", 0))
                .andExpect(cookie().maxAge("REFRESH_TOKEN", 0))
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0));
    }

    @Test
    void googleFirstSignInCreatesUserAndProfileReturnsUserRole() throws Exception {
        GoogleLoginRequest googleLoginRequest = new GoogleLoginRequest();
        googleLoginRequest.setIdToken("google-smoke-token");

        when(googleTokenVerifier.verify(Mockito.argThat(
                request -> "google-smoke-token".equals(request.getIdToken())
        ))).thenReturn(googlePayload("google-smoke@example.com"));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/googleSignIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JWT_TOKEN"))
                .andExpect(cookie().exists("REFRESH_TOKEN"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andReturn();

        User googleUser = userRepository.findByEmail("google-smoke@example.com").orElseThrow();
        assertTrue(googleUser.isEnabled());
        assertTrue(googleUser.getRoles().stream()
                .anyMatch(role -> UserRole.USER.name().equals(role.getRoleName())));

        mockMvc.perform(get("/api/user/userInfo")
                        .cookie(responseCookie(loginResult, "JWT_TOKEN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("google-smoke@example.com"))
                .andExpect(jsonPath("$.roles", hasItem(UserRole.USER.name())));
    }

    @Test
    void userCannotCreateProductButPromotedCreatorCanCreateAfterNewLogin() throws Exception {
        RegisterRequest register = registerVerifiedUser("promote-smoke@example.com");

        MvcResult userLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest(register.getEmail(), register.getPassword()))))
                .andExpect(status().isOk())
                .andReturn();

        User user = userRepository.findByEmail(register.getEmail()).orElseThrow();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(
                                responseCookie(userLogin, "JWT_TOKEN"),
                                responseCookie(userLogin, "XSRF-TOKEN")
                        )
                        .header("X-XSRF-TOKEN", responseCookie(userLogin, "XSRF-TOKEN").getValue())
                        .content(objectMapper.writeValueAsString(courseRequest(user.getUserId().toString(), "Blocked course"))))
                .andExpect(status().isForbidden());

        user.setRoles(Set.of(creatorRole));
        userRepository.saveAndFlush(user);

        MvcResult creatorLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest(register.getEmail(), register.getPassword()))))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/api/user/userInfo")
                        .cookie(responseCookie(creatorLogin, "JWT_TOKEN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem(UserRole.CREATOR.name())));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(
                                responseCookie(creatorLogin, "JWT_TOKEN"),
                                responseCookie(creatorLogin, "XSRF-TOKEN")
                        )
                        .header("X-XSRF-TOKEN", responseCookie(creatorLogin, "XSRF-TOKEN").getValue())
                        .content(objectMapper.writeValueAsString(courseRequest(user.getUserId().toString(), "Allowed course"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("COURSE"))
                .andExpect(jsonPath("$.name").value("Allowed course"));
    }

    @Test
    void publicExploreProductAndStoreProductReadsWorkWithoutAuthentication() throws Exception {
        RegisterRequest register = registerVerifiedUser("public-reads-creator@example.com");
        User creator = userRepository.findByEmail(register.getEmail()).orElseThrow();
        creator.setRoles(Set.of(creatorRole));
        userRepository.saveAndFlush(creator);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest(register.getEmail(), register.getPassword()))))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult productResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(
                                responseCookie(loginResult, "JWT_TOKEN"),
                                responseCookie(loginResult, "XSRF-TOKEN")
                        )
                        .header("X-XSRF-TOKEN", responseCookie(loginResult, "XSRF-TOKEN").getValue())
                        .content(objectMapper.writeValueAsString(courseRequest(creator.getUserId().toString(), "Public smoke course"))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdProduct = objectMapper.readTree(productResult.getResponse().getContentAsString());
        String productId = createdProduct.path("id").asText();

        mockMvc.perform(get("/api/products/get-all-products-min"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/products/search")
                        .param("term", "Public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId));

        mockMvc.perform(get("/api/products")
                        .param("ownerId", creator.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/products")
                        .param("userId", creator.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private RegisterRequest registerVerifiedUser(String email) throws Exception {
        RegisterRequest register = registerRequest(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(csrfCookie())
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        VerificationToken verificationToken = verificationTokenRepository.findAll().stream()
                .filter(token -> email.equals(token.getUser().getEmail()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/auth/verify")
                        .param("token", verificationToken.getToken()))
                .andExpect(status().isOk());

        return register;
    }

    private RegisterRequest registerRequest(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alexandra");
        request.setLastName("Smoke");
        request.setEmail(email);
        request.setPassword("Password123");
        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private CourseProductRequestDto courseRequest(String userId, String name) {
        CourseProductRequestDto request = new CourseProductRequestDto();
        request.setType("COURSE");
        request.setName(name);
        request.setDescription("Smoke test course");
        request.setStatus("DRAFT");
        request.setPrice("free");
        request.setUserId(userId);
        return request;
    }

    private GoogleIdToken.Payload googlePayload(String email) {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail(email);
        payload.setEmailVerified(true);
        payload.set("given_name", "Google");
        payload.set("family_name", "Smoke");
        payload.set("name", "Google Smoke");
        return payload;
    }

    private Cookie responseCookie(MvcResult result, String name) {
        Cookie cookie = result.getResponse().getCookie(name);
        assertNotNull(cookie, () -> "Expected response cookie " + name);
        return cookie;
    }

    private Cookie csrfCookie() {
        return new Cookie("XSRF-TOKEN", CSRF);
    }
}

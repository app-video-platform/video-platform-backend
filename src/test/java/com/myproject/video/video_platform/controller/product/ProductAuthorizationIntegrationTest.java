package com.myproject.video.video_platform.controller.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.dto.products.course.CourseProductRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductDetailsRequestDto;
import com.myproject.video.video_platform.dto.products.download.DownloadProductRequestDto;
import com.myproject.video.video_platform.dto.products.download.SectionDownloadProductRequestDto;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.course.CourseLessonRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.course.CourseSectionRepository;
import com.myproject.video.video_platform.repository.products.download.DownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download.FileDownloadProductRepository;
import com.myproject.video.video_platform.repository.products.download.SectionDownloadProductRepository;
import com.myproject.video.video_platform.service.digitalocean.SpacesS3Service;
import com.myproject.video.video_platform.service.security.JwtProvider;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductAuthorizationIntegrationTest {

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
    private SpacesS3Service spacesS3Service;

    private Role userRole;
    private Role creatorRole;

    @BeforeEach
    void setUp() {
        Mockito.when(spacesS3Service.generatePresignedUrlForPut(Mockito.anyString(), Mockito.any()))
                .thenReturn("https://signed.example.com/upload");

        fileDownloadProductRepository.deleteAll();
        sectionDownloadProductRepository.deleteAll();
        downloadProductRepository.deleteAll();
        courseLessonRepository.deleteAll();
        courseSectionRepository.deleteAll();
        courseProductRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        userRole = roleRepository.save(new Role(null, UserRole.USER.name()));
        creatorRole = roleRepository.save(new Role(null, UserRole.CREATOR.name()));
    }

    @Test
    void publicProductReadEndpointWorksWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/products/get-all-products-min"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void productWriteRequiresAuthenticatedCreatorOrAdminRole() throws Exception {
        CourseProductRequestDto dto = courseRequest();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("XSRF-TOKEN", CSRF))
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError());

        User normalUser = persistUser("normal@example.com", userRole);
        dto.setUserId(normalUser.getUserId().toString());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(normalUser), new Cookie("XSRF-TOKEN", CSRF))
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());

        User creator = persistUser("creator@example.com", creatorRole);
        dto.setUserId(creator.getUserId().toString());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(creator), new Cookie("XSRF-TOKEN", CSRF))
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void userInfoReturnsCanonicalUppercaseRoles() throws Exception {
        User user = persistUser("profile@example.com", userRole);

        mockMvc.perform(get("/api/user/userInfo")
                        .cookie(authCookie(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasItem(UserRole.USER.name())));
    }

    @Test
    void presignedUploadEndpointRequiresCreatorOrAdminRole() throws Exception {
        User normalUser = persistUser("normal@example.com", userRole);

        mockMvc.perform(get("/api/files/presigned-url")
                        .param("sectionId", UUID.randomUUID().toString())
                        .param("folderType", "DOWNLOAD_SECTION_FILES")
                        .param("filename", "resource.pdf")
                        .cookie(authCookie(normalUser)))
                .andExpect(status().isForbidden());

        User creator = persistUser("creator@example.com", creatorRole);
        JsonNode product = createDownloadProduct(creator);
        String sectionId = product.path("details").path("sections").get(0).path("id").asText();

        mockMvc.perform(get("/api/files/presigned-url")
                        .param("sectionId", sectionId)
                        .param("folderType", "DOWNLOAD_SECTION_FILES")
                        .param("filename", "resource.pdf")
                        .cookie(authCookie(creator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presignedUrl").value("https://signed.example.com/upload"));
    }

    private JsonNode createDownloadProduct(User creator) throws Exception {
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
        dto.setUserId(creator.getUserId().toString());
        dto.setDetails(details);

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(authCookie(creator), new Cookie("XSRF-TOKEN", CSRF))
                        .header("X-XSRF-TOKEN", CSRF)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
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

    private Cookie authCookie(User user) {
        return new Cookie("JWT_TOKEN", jwtProvider.generateToken(user));
    }
}

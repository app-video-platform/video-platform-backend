package com.myproject.video.video_platform.controller.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.products.membership.MembershipProduct;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.repository.products.membership.MembershipContentRepository;
import com.myproject.video.video_platform.repository.products.membership.MembershipFeedEntryRepository;
import com.myproject.video.video_platform.repository.products.membership.MembershipProductRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import com.myproject.video.video_platform.service.admin.AdminProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(roles = "CREATOR")
class MembershipControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private CourseProductRepository courseRepository;
    @Autowired private MembershipProductRepository membershipRepository;
    @Autowired private MembershipContentRepository contentRepository;
    @Autowired private MembershipFeedEntryRepository feedRepository;
    @Autowired private AdminProductService adminProductService;
    @MockBean private CurrentUserService currentUserService;

    private final AtomicReference<UUID> currentUserId = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        Mockito.when(currentUserService.getCurrentUserId()).thenAnswer(invocation -> currentUserId.get());
        clean();
    }

    @AfterEach
    void tearDown() {
        clean();
    }

    @Test
    void membershipProductDefaultsPersistAndPublishingIsBlocked() throws Exception {
        User owner = persistUser("membership-owner@example.com");
        currentUserId.set(owner.getUserId());

        UUID membershipId = createMembership(owner, "Creator circle");

        mockMvc.perform(get("/api/products/{id}", membershipId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("MEMBERSHIP"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.price").value("free"))
                .andExpect(jsonPath("$.pricingModel").value("RECURRING"))
                .andExpect(jsonPath("$.billingInterval").value("MONTH"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.details").value(nullValue()));

        mockMvc.perform(patch("/api/products/{id}", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":\"29.95\",\"billingInterval\":\"YEAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value("29.95"))
                .andExpect(jsonPath("$.billingInterval").value("YEAR"));

        mockMvc.perform(patch("/api/products/{id}/membership", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderingMode\":\"MANUAL\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.config.orderingMode").value("MANUAL"));
        mockMvc.perform(patch("/api/products/{id}/membership", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderingMode\":\"NEWEST_FIRST\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.config.orderingMode").value("NEWEST_FIRST"));

        mockMvc.perform(patch("/api/products/{id}", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.status").exists());

        MembershipProduct reloaded = membershipRepository.findById(membershipId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("29.95", reloaded.getPrice().toPlainString());
        org.junit.jupiter.api.Assertions.assertEquals(1,
                adminProductService.searchProducts(
                        null, null, ProductType.MEMBERSHIP, null, PageRequest.of(0, 10))
                        .getTotalElements());

        mockMvc.perform(get("/api/products").param("ownerId", owner.getUserId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("MEMBERSHIP"))
                .andExpect(jsonPath("$[0].pricingModel").value("RECURRING"));
        mockMvc.perform(get("/api/products/search")
                        .param("term", "creator circle").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(membershipId.toString()));
    }

    @Test
    void nativeContentPersistsMetadataAndFeedOrdering() throws Exception {
        User owner = persistUser("content-owner@example.com");
        currentUserId.set(owner.getUserId());
        UUID membershipId = createMembership(owner, "Content membership");

        UUID postId = createContent(membershipId, """
                {"type":"POST","title":"Welcome","body":"Hello members","status":"PUBLISHED"}
                """);
        String clientFileId = UUID.randomUUID().toString();
        String videoResponse = mockMvc.perform(post("/api/products/{id}/membership/content", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"VIDEO","title":"Lesson","description":"Watch", "status":"DRAFT",
                                 "video":{"fileId":"%s","fileName":"lesson.mp4","fileType":"video/mp4","size":1234,
                                          "url":"https://untrusted.example/video"}}
                                """.formatted(clientFileId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.video.fileId", not(clientFileId)))
                .andExpect(jsonPath("$.video.url").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        UUID videoId = UUID.fromString(objectMapper.readTree(videoResponse).get("id").asText());
        UUID resourceId = createContent(membershipId, """
                {"type":"RESOURCE","title":"Workbook","description":"Download metadata",
                 "status":"HIDDEN","file":{"fileName":"workbook.pdf","fileType":"application/pdf","size":4567,
                 "url":"https://untrusted.example/workbook"}}
                """);

        mockMvc.perform(patch("/api/products/{id}/membership/content/{contentId}", membershipId, postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"POST\",\"title\":\"Welcome back\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Welcome back"))
                .andExpect(jsonPath("$.body").value("Hello members"));

        mockMvc.perform(patch("/api/products/{id}/membership/content/{contentId}", membershipId, postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"VIDEO\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/products/{id}/membership/feed", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderingMode":"MANUAL","feed":[
                                  {"entryId":"ignored","kind":"CONTENT","contentId":"%s","addedAt":"2000-01-01T00:00:00Z"},
                                  {"entryId":"ignored","kind":"CONTENT","contentId":"%s","addedAt":"2000-01-01T00:00:00Z"},
                                  {"entryId":"ignored","kind":"CONTENT","contentId":"%s","addedAt":"2000-01-01T00:00:00Z"}
                                ]}
                                """.formatted(postId, videoId, resourceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.config.orderingMode").value("MANUAL"))
                .andExpect(jsonPath("$.feed[0].entryId").value("content:" + postId))
                .andExpect(jsonPath("$.feed[0].position").value(1))
                .andExpect(jsonPath("$.feed[1].position").value(2))
                .andExpect(jsonPath("$.feed[2].position").value(3));

        mockMvc.perform(put("/api/products/{id}/membership/feed", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderingMode":"NEWEST_FIRST","feed":[
                                  {"kind":"CONTENT","contentId":"%s"},
                                  {"kind":"CONTENT","contentId":"%s"},
                                  {"kind":"CONTENT","contentId":"%s"}
                                ]}
                                """.formatted(postId, videoId, resourceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.config.orderingMode").value("NEWEST_FIRST"))
                .andExpect(jsonPath("$.feed[0].entryId").value("content:" + resourceId))
                .andExpect(jsonPath("$.feed[0].position").doesNotExist());

        mockMvc.perform(delete("/api/products/{id}/membership/content/{contentId}", membershipId, postId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/products/{id}/membership", membershipId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.feed", hasSize(2)));

        mockMvc.perform(delete("/api/products/{id}", membershipId))
                .andExpect(status().isNoContent());
        org.junit.jupiter.api.Assertions.assertEquals(0, contentRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, feedRepository.count());
    }

    @Test
    void includedProductsAreValidatedAndReferencesAreRemovedOnDeletion() throws Exception {
        User owner = persistUser("included-owner@example.com");
        User other = persistUser("included-other@example.com");
        currentUserId.set(owner.getUserId());
        UUID membershipId = createMembership(owner, "Bundle");
        CourseProduct course = createCourse(owner, "Included course");
        CourseProduct otherCourse = createCourse(other, "Other course");

        mockMvc.perform(put("/api/products/{id}/membership/feed", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedWithProduct(course.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feed[0].entryId").value("product:" + course.getId()));

        mockMvc.perform(put("/api/products/{id}/membership/feed", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedWithProduct(otherCourse.getId())))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/products/{id}/membership/feed", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedWithProduct(UUID.randomUUID())))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/products/{id}/membership/feed", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedWithProduct(membershipId)))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/products/{id}/membership/feed", membershipId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderingMode":"MANUAL","feed":[
                                  {"kind":"PRODUCT","productId":"%s"},
                                  {"kind":"PRODUCT","productId":"%s"}
                                ]}
                                """.formatted(course.getId(), course.getId())))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/products/{id}", course.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/products/{id}/membership", membershipId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feed", hasSize(0)));
    }

    @Test
    void ownerAdminAndWrongTypeBoundariesAreEnforced() throws Exception {
        User owner = persistUser("auth-owner@example.com");
        User other = persistUser("auth-other@example.com");
        currentUserId.set(owner.getUserId());
        UUID membershipId = createMembership(owner, "Private authoring");
        CourseProduct course = createCourse(owner, "Wrong type");

        currentUserId.set(other.getUserId());
        mockMvc.perform(get("/api/products/{id}/membership", membershipId))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/products/{id}/membership", course.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadAnotherCreatorsMembership() throws Exception {
        User owner = persistUser("admin-owner@example.com");
        currentUserId.set(owner.getUserId());
        UUID membershipId = persistMembership(owner, "Admin visible").getId();
        currentUserId.set(UUID.randomUUID());

        mockMvc.perform(get("/api/products/{id}/membership", membershipId))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void anonymousCannotReadMembershipAuthoringAggregate() throws Exception {
        User owner = persistUser("anonymous-owner@example.com");
        currentUserId.set(owner.getUserId());
        MembershipProduct membership = persistMembership(owner, "Private");

        mockMvc.perform(get("/api/products/{id}/membership", membership.getId()))
                .andExpect(status().isForbidden());
    }

    private UUID createMembership(User owner, String name) throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "MEMBERSHIP", "name", name, "status", "DRAFT",
                                "userId", owner.getUserId().toString()
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID createContent(UUID membershipId, String payload) throws Exception {
        JsonNode response = objectMapper.readTree(mockMvc.perform(
                        post("/api/products/{id}/membership/content", membershipId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
        return UUID.fromString(response.get("id").asText());
    }

    private CourseProduct createCourse(User owner, String name) {
        CourseProduct product = new CourseProduct();
        product.setName(name);
        product.setDescription("Description");
        product.setStatus(ProductStatus.DRAFT);
        product.setType(ProductType.COURSE);
        product.setPrice(BigDecimal.ZERO);
        product.setUser(owner);
        return courseRepository.save(product);
    }

    private MembershipProduct persistMembership(User owner, String name) {
        MembershipProduct membership = new MembershipProduct();
        membership.setName(name);
        membership.setDescription("Private");
        membership.setType(ProductType.MEMBERSHIP);
        membership.setStatus(ProductStatus.DRAFT);
        membership.setPrice(BigDecimal.ZERO);
        membership.setPricingModel(com.myproject.video.video_platform.common.enums.products.ProductPricingModel.RECURRING);
        membership.setBillingInterval(com.myproject.video.video_platform.common.enums.products.ProductBillingInterval.MONTH);
        membership.setCurrency(com.myproject.video.video_platform.common.enums.products.ProductCurrency.EUR);
        membership.setUser(owner);
        return membershipRepository.save(membership);
    }

    private String feedWithProduct(UUID productId) {
        return """
                {"orderingMode":"MANUAL","feed":[{"kind":"PRODUCT","productId":"%s"}]}
                """.formatted(productId);
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

    private void clean() {
        feedRepository.deleteAll();
        contentRepository.deleteAll();
        membershipRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
    }
}

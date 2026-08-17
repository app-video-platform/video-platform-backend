package com.myproject.video.video_platform.controller.commerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.video.video_platform.common.enums.entitlement.EntitlementStatus;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.entity.entitlement.ProductEntitlement;
import com.myproject.video.video_platform.entity.products.course.CourseProduct;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.UnsupportedProductOperationException;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import com.myproject.video.video_platform.repository.commerce.CommerceOrderRepository;
import com.myproject.video.video_platform.repository.commerce.CommercePaymentAttemptRepository;
import com.myproject.video.video_platform.repository.commerce.CommercePaymentEventRepository;
import com.myproject.video.video_platform.repository.entitlement.ProductEntitlementRepository;
import com.myproject.video.video_platform.repository.products.course.CourseProductRepository;
import com.myproject.video.video_platform.service.product.ProductService;
import com.myproject.video.video_platform.service.security.JwtProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommerceCheckoutIntegrationTest {

    private static final String CSRF = "commerce-test-csrf";

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
    private CourseProductRepository productRepository;
    @Autowired
    private ProductEntitlementRepository entitlementRepository;
    @Autowired
    private CommercePaymentEventRepository paymentEventRepository;
    @Autowired
    private CommercePaymentAttemptRepository paymentAttemptRepository;
    @Autowired
    private CommerceOrderRepository orderRepository;
    @Autowired
    private ProductService productService;

    private Role userRole;
    private Role creatorRole;
    private Role adminRole;
    private User buyer;
    private User creator;
    private User admin;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        userRole = roleRepository.save(new Role(null, UserRole.USER.name()));
        creatorRole = roleRepository.save(new Role(null, UserRole.CREATOR.name()));
        adminRole = roleRepository.save(new Role(null, UserRole.ADMIN.name()));
        buyer = persistUser("buyer@example.com", userRole);
        creator = persistUser("creator@example.com", creatorRole);
        admin = persistUser("admin@example.com", adminRole);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        paymentEventRepository.deleteAll();
        entitlementRepository.deleteAll();
        paymentAttemptRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void fakePaymentCompletesIdempotentlyAndGrantsPurchaseEntitlements() throws Exception {
        CourseProduct first = createCourse(creator, "First", "19.99", ProductStatus.PUBLISHED);
        CourseProduct second = createCourse(creator, "Second", "10.00", ProductStatus.PUBLISHED);

        String response = createCheckout(List.of(first.getId(), second.getId()), "checkout-1")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.provider").value("FAKE"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.totalMinor").value(2999))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        UUID orderId = UUID.fromString(objectMapper.readTree(response).get("orderId").asText());

        createCheckout(List.of(second.getId(), first.getId()), "checkout-1")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));

        simulate(orderId, "PAID")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
        simulate(orderId, "PAID")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        List<ProductEntitlement> entitlements = entitlementRepository.findAll();
        assertEquals(2, entitlements.size());
        entitlements.forEach(entitlement -> {
            assertEquals("PURCHASE", entitlement.getSource().name());
            assertEquals(EntitlementStatus.ACTIVE, entitlement.getStatus());
            assertNotNull(entitlement.getPurchaseOrderItemId());
        });

        mockMvc.perform(get("/api/commerce/orders/{orderId}", orderId)
                        .cookie(authCookie(buyer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void fullRefundRevokesOnlyPurchaseAccess() throws Exception {
        CourseProduct product = createCourse(creator, "Refundable", "25.00", ProductStatus.PUBLISHED);
        UUID orderId = checkoutOrderId(product, "refund-checkout");

        simulate(orderId, "PAID").andExpect(status().isOk());
        simulate(orderId, "REFUNDED")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));

        ProductEntitlement entitlement = entitlementRepository.findAll().get(0);
        assertEquals(EntitlementStatus.REVOKED, entitlement.getStatus());
        assertNotNull(entitlement.getRevokedAt());
    }

    @Test
    void invalidCheckoutRulesAndIdempotencyConflictAreEnforced() throws Exception {
        User anotherCreator = persistUser("creator-two@example.com", creatorRole);
        CourseProduct first = createCourse(creator, "First", "12.00", ProductStatus.PUBLISHED);
        CourseProduct second = createCourse(anotherCreator, "Second", "8.00", ProductStatus.PUBLISHED);
        CourseProduct free = createCourse(creator, "Free", "0.00", ProductStatus.PUBLISHED);

        createCheckout(List.of(first.getId(), second.getId()), "mixed-creators")
                .andExpect(status().isUnprocessableEntity());
        createCheckout(List.of(free.getId()), "free-product")
                .andExpect(status().isUnprocessableEntity());
        createCheckout(List.of(first.getId(), first.getId()), "duplicate-product")
                .andExpect(status().isUnprocessableEntity());

        createCheckout(List.of(first.getId()), "reused-key")
                .andExpect(status().isCreated());
        createCheckout(List.of(second.getId()), "reused-key")
                .andExpect(status().isConflict());
    }

    @Test
    void unrelatedUserCannotReadOrderAndPendingOrderBlocksProductDeletion() throws Exception {
        CourseProduct product = createCourse(creator, "Protected", "15.00", ProductStatus.PUBLISHED);
        UUID orderId = checkoutOrderId(product, "protected-checkout");
        User unrelated = persistUser("other@example.com", userRole);

        mockMvc.perform(get("/api/commerce/orders/{orderId}", orderId)
                        .cookie(authCookie(unrelated)))
                .andExpect(status().isForbidden());

        assertThrows(
                UnsupportedProductOperationException.class,
                () -> productService.deleteProductById(product.getId().toString())
        );
    }

    private UUID checkoutOrderId(CourseProduct product, String idempotencyKey) throws Exception {
        String response = createCheckout(List.of(product.getId()), idempotencyKey)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return UUID.fromString(json.get("orderId").asText());
    }

    private org.springframework.test.web.servlet.ResultActions createCheckout(
            List<UUID> productIds,
            String idempotencyKey
    ) throws Exception {
        return mockMvc.perform(post("/api/commerce/checkout-sessions")
                .cookie(authCookie(buyer), csrfCookie())
                .header("X-XSRF-TOKEN", CSRF)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("productIds", productIds))));
    }

    private org.springframework.test.web.servlet.ResultActions simulate(UUID orderId, String outcome)
            throws Exception {
        return mockMvc.perform(post("/api/dev/commerce/orders/{orderId}/simulate", orderId)
                .cookie(authCookie(admin), csrfCookie())
                .header("X-XSRF-TOKEN", CSRF)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"outcome\":\"" + outcome + "\"}"));
    }

    private CourseProduct createCourse(
            User owner,
            String name,
            String price,
            ProductStatus status
    ) {
        CourseProduct product = new CourseProduct();
        product.setName(name);
        product.setDescription("Description");
        product.setStatus(status);
        product.setType(ProductType.COURSE);
        product.setPrice(new BigDecimal(price));
        product.setUser(owner);
        return productRepository.save(product);
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

    private Cookie csrfCookie() {
        return new Cookie("XSRF-TOKEN", CSRF);
    }
}

package com.myproject.video.video_platform.service.creator;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.common.enums.entitlement.EntitlementSource;
import com.myproject.video.video_platform.common.enums.entitlement.EntitlementStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.commerce.CommerceOrderItem;
import com.myproject.video.video_platform.entity.entitlement.ProductEntitlement;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.creator.CreatorReportingQueryRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CreatorReportingServicesTest {
    private static final UUID CREATOR_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-08-22T12:00:00Z");

    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private CreatorReportingQueryRepository reportingRepository;

    private CreatorSalesService salesService;
    private CreatorCustomersService customersService;
    private CreatorAnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        salesService = new CreatorSalesService(currentUserService, reportingRepository, clock);
        customersService = new CreatorCustomersService(currentUserService, reportingRepository);
        analyticsService = new CreatorAnalyticsService(currentUserService, reportingRepository, clock);
        lenient().when(currentUserService.getCurrentUserId()).thenReturn(CREATOR_ID);
    }

    @Test
    void salesUsesEventWindowsAndReturnsAuthoritativeMultiItemOrders() {
        User buyer = user("Maya", "Buyer", "maya@example.test");
        CommerceOrder paid = order(buyer, CommerceOrderStatus.PAID, "2026-08-20T10:00:00Z", 12_000,
                item("Course", ProductType.COURSE, 7_000), item("Toolkit", ProductType.DOWNLOAD, 5_000));
        paid.setPaidAt(Instant.parse("2026-08-20T10:01:00Z"));
        CommerceOrder refunded = order(buyer, CommerceOrderStatus.REFUNDED, "2026-08-19T10:00:00Z", 4_900,
                item("Old toolkit", ProductType.DOWNLOAD, 4_900));
        refunded.setPaidAt(Instant.parse("2026-08-19T10:01:00Z"));
        refunded.setRefundedAt(Instant.parse("2026-08-21T09:00:00Z"));
        CommerceOrder failed = order(user("Only", "Failed", "failed@example.test"), CommerceOrderStatus.FAILED,
                "2026-08-20T08:00:00Z", 2_000, item("Course", ProductType.COURSE, 2_000));
        failed.setFailedAt(Instant.parse("2026-08-20T08:01:00Z"));
        CommerceOrder expired = order(buyer, CommerceOrderStatus.EXPIRED, "2026-08-20T08:00:00Z", 1_000,
                item("Expired", ProductType.DOWNLOAD, 1_000));
        stub(List.of(paid, refunded, failed, expired), List.of());

        var summary = salesService.summary("7d");
        assertEquals("EUR 120", summary.metrics().get(0).value());
        assertEquals("1", summary.metrics().get(1).value());
        assertEquals("EUR 49", summary.metrics().get(2).value());
        assertEquals("1", summary.metrics().get(3).value());

        var page = salesService.orders(0, 10, null, "all", null, "7d", "newest");
        assertEquals(3, page.totalElements());
        var multi = page.content().stream().filter(value -> value.id().equals(paid.getId().toString())).findFirst().orElseThrow();
        assertEquals(2, multi.items().size());
        assertNull(multi.product());
        assertEquals(buyer.getUserId().toString(), multi.customer().id());
        var failedResult = page.content().stream().filter(value -> value.id().equals(failed.getId().toString())).findFirst().orElseThrow();
        assertNull(failedResult.customer().id());
        assertEquals(1, salesService.orders(0, 10, "failed@example", "failed", null, "7d", "amount-asc").totalElements());
        assertEquals(1, salesService.orders(0, 10, null, "all",
                paid.getItems().get(0).getProductId().toString(), "7d", "newest").totalElements());
        assertEquals(3, salesService.orders(0, 1, null, "all", null, "7d", "newest").totalPages());
        assertThrows(ResourceNotFoundException.class, () -> salesService.order(UUID.randomUUID()));
    }

    @Test
    void customerPopulationIncludesFreeAccessAndExcludesFailedOnlyUsers() {
        User freeUser = user("Free", "Learner", "free@example.test");
        User failedUser = user("Failed", "Only", "failed@example.test");
        UUID productId = UUID.randomUUID();
        ProductEntitlement free = entitlement(freeUser, productId, EntitlementSource.FREE_ENROLLMENT);
        CommerceOrder failed = order(failedUser, CommerceOrderStatus.FAILED, "2026-08-20T08:00:00Z", 2_000,
                itemWithId(productId, "Course", ProductType.COURSE, 2_000));
        failed.setFailedAt(Instant.parse("2026-08-20T08:01:00Z"));
        CommerceOrder refunded = order(freeUser, CommerceOrderStatus.REFUNDED, "2026-08-17T08:00:00Z", 3_000,
                itemWithId(productId, "Course", ProductType.COURSE, 3_000));
        refunded.setPaidAt(Instant.parse("2026-08-17T08:01:00Z"));
        refunded.setRefundedAt(Instant.parse("2026-08-19T08:01:00Z"));
        stub(List.of(failed, refunded), List.of(free));

        var page = customersService.customers(0, 10, null, "buyer", null, "none", "name-asc");
        assertEquals(1, page.totalElements());
        assertEquals(freeUser.getUserId().toString(), page.content().get(0).id());
        var detail = customersService.customer(freeUser.getUserId());
        assertEquals("free", detail.access().get(0).source());
        assertEquals("buyer", detail.relationshipStatus());
        assertEquals("none", detail.membershipState());
        assertEquals(Instant.parse("2026-08-17T08:01:00Z"), detail.purchases().get(0).purchasedAt());
        assertTrue(detail.activity().stream().anyMatch(activity -> activity.label().equals("Purchase completed")));
        assertTrue(detail.activity().stream().anyMatch(activity -> activity.label().equals("Order refunded")));
        assertEquals(1, customersService.customers(0, 10, null, "all", productId.toString(),
                "all", "last-activity-desc").totalElements());
        assertTrue(customersService.customers(0, 10, null, "active-member", null, "all", null).empty());
        assertThrows(ResourceNotFoundException.class, () -> customersService.customer(failedUser.getUserId()));
    }

    @Test
    void analyticsReturnsDeterministicBucketsAndTruthfulEmptyMembershipData() {
        User buyer = user("Paid", "Buyer", "paid@example.test");
        CommerceOrder paid = order(buyer, CommerceOrderStatus.PAID, "2026-08-20T10:00:00Z", 10_000,
                item("Course", ProductType.COURSE, 10_000));
        paid.setPaidAt(Instant.parse("2026-08-20T10:01:00Z"));
        stub(List.of(paid), List.of());

        var sevenDays = analyticsService.overview("7d");
        assertEquals(7, sevenDays.performance().series().size());
        assertEquals(10_000, sevenDays.performance().series().stream().mapToLong(point -> point.revenue()).sum());
        assertEquals(1, sevenDays.products().get(0).orders());
        assertNull(sevenDays.memberships().summary());
        assertTrue(sevenDays.memberships().series().isEmpty());

        var ninetyDays = analyticsService.overview("90d");
        assertEquals(13, ninetyDays.performance().series().size());
        assertThrows(IllegalArgumentException.class, () -> analyticsService.overview("today"));
    }

    @Test
    void invalidFiltersAndPaginationAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> salesService.orders(-1, 10, null, "all", null, "7d", "newest"));
        assertThrows(IllegalArgumentException.class,
                () -> salesService.orders(0, 101, null, "all", null, "7d", "newest"));
        assertThrows(IllegalArgumentException.class,
                () -> customersService.customers(0, 10, null, "unknown", null, "all", null));
    }

    private void stub(List<CommerceOrder> orders, List<ProductEntitlement> entitlements) {
        when(reportingRepository.load(CREATOR_ID)).thenReturn(new CreatorReportingQueryRepository.ReportingData(
                orders, List.of(), entitlements, Map.of()));
    }

    private static User user(String firstName, String lastName, String email) {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        return user;
    }

    private static CommerceOrder order(
            User buyer, CommerceOrderStatus status, String createdAt, long total, CommerceOrderItem... items
    ) {
        CommerceOrder order = new CommerceOrder();
        order.setId(UUID.randomUUID());
        order.setBuyer(buyer);
        order.setStatus(status);
        order.setCurrency("EUR");
        order.setSubtotalMinor(total);
        order.setTotalMinor(total);
        order.setCreatedAt(Instant.parse(createdAt));
        order.setExpiresAt(Instant.parse(createdAt).plusSeconds(3_600));
        for (CommerceOrderItem item : items) order.addItem(item);
        return order;
    }

    private static CommerceOrderItem item(String name, ProductType type, long amount) {
        return itemWithId(UUID.randomUUID(), name, type, amount);
    }

    private static CommerceOrderItem itemWithId(UUID productId, String name, ProductType type, long amount) {
        CommerceOrderItem item = new CommerceOrderItem();
        item.setId(UUID.randomUUID());
        item.setProductId(productId);
        item.setProductName(name);
        item.setProductType(type);
        item.setUnitAmountMinor(amount);
        item.setLineTotalMinor(amount);
        item.setQuantity(1);
        return item;
    }

    private static ProductEntitlement entitlement(User user, UUID productId, EntitlementSource source) {
        ProductEntitlement entitlement = new ProductEntitlement();
        entitlement.setId(UUID.randomUUID());
        entitlement.setUser(user);
        entitlement.setProductId(productId);
        entitlement.setProductType(ProductType.COURSE);
        entitlement.setStatus(EntitlementStatus.ACTIVE);
        entitlement.setSource(source);
        entitlement.setCreatedAt(Instant.parse("2026-08-18T10:00:00Z"));
        return entitlement;
    }
}

package com.myproject.video.video_platform.service.creator;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.common.enums.products.ProductStatus;
import com.myproject.video.video_platform.dto.creator.dashboard.CreatorDashboardDtos;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.repository.creator.CreatorReportingQueryRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreatorDashboardService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneOffset.UTC);
    private final CurrentUserService currentUserService;
    private final CreatorReportingQueryRepository reportingRepository;
    private final Clock reportingClock;

    @Transactional(readOnly = true)
    public CreatorDashboardDtos.Summary summary() {
        var data = reportingRepository.load(currentUserService.getCurrentUserId());
        ReportingWindow window = ReportingWindow.forPeriod(ReportingEnums.Period.DAYS_30, reportingClock);
        long revenue = revenue(data.orders(), window, false);
        long previousRevenue = revenue(data.orders(), window, true);
        long sales = paidOrders(data.orders(), window, false);
        long previousSales = paidOrders(data.orders(), window, true);
        Map<UUID, Instant> relationships = relationships(data);
        long newCustomers = relationships.values().stream().filter(window::current).count();
        String currency = data.orders().stream().map(CommerceOrder::getCurrency).findFirst().orElse("EUR");

        return new CreatorDashboardDtos.Summary(
                metrics(revenue, previousRevenue, sales, previousSales, relationships.size(), newCustomers, currency),
                activities(data, reportingClock.instant()), topProducts(data, window, currency), attention(data, window));
    }

    private static List<CreatorDashboardDtos.Metric> metrics(long revenue, long previousRevenue, long sales,
            long previousSales, long customers, long newCustomers, String currency) {
        var revenueComparison = CreatorReportingSupport.comparison(revenue, previousRevenue, false);
        var salesComparison = CreatorReportingSupport.comparison(sales, previousSales, false);
        return List.of(
                metric("revenue", "Revenue", CreatorReportingSupport.money(revenue, currency), revenueComparison, "/app/sales"),
                metric("sales", "Sales", Long.toString(sales), salesComparison, "/app/sales"),
                new CreatorDashboardDtos.Metric("customers", "Customers", Long.toString(customers),
                        "+" + newCustomers + " this period", newCustomers > 0 ? "up" : "flat",
                        newCustomers > 0 ? "favorable" : "neutral", "ready", "/app/customers"),
                new CreatorDashboardDtos.Metric("active-memberships", "Active memberships", "—", null, null,
                        "neutral", "unavailable", "/app/analytics")
        );
    }

    private static CreatorDashboardDtos.Metric metric(String id, String label, String value,
            CreatorReportingSupport.Comparison comparison, String path) {
        return new CreatorDashboardDtos.Metric(id, label, value, comparison.text(), comparison.direction(),
                comparison.sentiment(), "ready", path);
    }

    private static List<CreatorDashboardDtos.TopProduct> topProducts(
            CreatorReportingQueryRepository.ReportingData data, ReportingWindow window, String currency) {
        Map<UUID, ProductTotal> totals = new LinkedHashMap<>();
        Map<UUID, Product> products = CreatorReportingSupport.productsById(data.products());
        data.orders().stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID)
                .filter(order -> window.current(order.getPaidAt())).forEach(order -> order.getItems().forEach(item -> {
                    Product current = products.get(item.getProductId());
                    ProductTotal total = totals.computeIfAbsent(item.getProductId(), ignored -> new ProductTotal(
                            item.getProductId(), item.getProductName(), CreatorReportingSupport.productType(item.getProductType()),
                            current == null ? null : current.getImage()));
                    total.revenue += item.getLineTotalMinor();
                }));
        List<ProductTotal> ranked = totals.values().stream()
                .sorted(Comparator.comparingLong(ProductTotal::revenue).reversed()).limit(5).toList();
        long leader = ranked.isEmpty() ? 0 : ranked.get(0).revenue;
        return ranked.stream().map(product -> new CreatorDashboardDtos.TopProduct(product.id.toString(), product.name,
                product.type, CreatorReportingSupport.money(product.revenue, currency), leader == 0 ? 0
                : BigDecimal.valueOf(product.revenue * 100.0 / leader).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                product.image, "/app/products/" + product.id)).toList();
    }

    private static List<CreatorDashboardDtos.Activity> activities(
            CreatorReportingQueryRepository.ReportingData data, Instant now) {
        List<TimedActivity> activities = new ArrayList<>();
        for (CommerceOrder order : data.orders()) {
            Instant timestamp = switch (order.getStatus()) {
                case PAID -> order.getPaidAt();
                case FAILED -> order.getFailedAt();
                case REFUNDED -> order.getRefundedAt();
                default -> null;
            };
            if (timestamp == null) continue;
            String products = order.getItems().stream().map(item -> item.getProductName()).collect(Collectors.joining(", "));
            String path = "/app/sales?order=" + order.getId();
            if (order.getStatus() == CommerceOrderStatus.FAILED) {
                activities.add(new TimedActivity(timestamp, new CreatorDashboardDtos.Activity(order.getId().toString(),
                        "failed-payment", "Payment failed", products, CreatorReportingSupport.money(order.getTotalMinor(), order.getCurrency()),
                        "Failed", relative(timestamp, now), path)));
            } else {
                String status = order.getStatus() == CommerceOrderStatus.REFUNDED ? "Refunded" : "Paid";
                String title = order.getStatus() == CommerceOrderStatus.REFUNDED ? "Order refunded" : "New sale";
                activities.add(new TimedActivity(timestamp, new CreatorDashboardDtos.Activity(order.getId().toString(),
                        "sale", title, products, CreatorReportingSupport.money(order.getTotalMinor(), order.getCurrency()),
                        status, relative(timestamp, now), path)));
            }
        }
        for (Product product : data.products()) {
            if (product.getUpdatedAt() == null) continue;
            Instant timestamp = product.getUpdatedAt().toInstant(ZoneOffset.UTC);
            activities.add(new TimedActivity(timestamp, new CreatorDashboardDtos.Activity("product-" + product.getId(),
                    "product-updated", "Product updated", product.getName(), null, null,
                    relative(timestamp, now), "/app/products/" + product.getId())));
        }
        return activities.stream().sorted(Comparator.comparing(TimedActivity::timestamp).reversed()).limit(10)
                .map(TimedActivity::activity).toList();
    }

    private static List<CreatorDashboardDtos.AttentionItem> attention(
            CreatorReportingQueryRepository.ReportingData data, ReportingWindow window) {
        List<CreatorDashboardDtos.AttentionItem> result = new ArrayList<>();
        long failures = data.orders().stream().filter(order -> order.getStatus() == CommerceOrderStatus.FAILED)
                .filter(order -> window.current(order.getFailedAt())).count();
        if (failures > 0) result.add(new CreatorDashboardDtos.AttentionItem("failed-payments", "Failed payments",
                failures + " payment" + (failures == 1 ? "" : "s") + " failed in the last 30 days", "high",
                "Review sales", "/app/sales", null));
        long drafts = data.products().stream().filter(product -> product.getStatus() == ProductStatus.DRAFT).count();
        if (drafts > 0) result.add(new CreatorDashboardDtos.AttentionItem("draft-products", "Draft Products",
                drafts + " Product" + (drafts == 1 ? " is" : "s are") + " still in draft", "low",
                "Review Products", "/app/products", null));
        List<Product> missingImages = data.products().stream().filter(product -> product.getStatus() == ProductStatus.PUBLISHED)
                .filter(product -> product.getImage() == null || product.getImage().isBlank()).toList();
        if (!missingImages.isEmpty()) result.add(new CreatorDashboardDtos.AttentionItem("missing-product-images",
                "Published Products missing an image", missingImages.size() + " published Product" +
                (missingImages.size() == 1 ? " needs" : "s need") + " an image", "medium", "Add images",
                missingImages.size() == 1 ? "/app/products/edit/" + missingImages.get(0).getId() : "/app/products", null));
        return List.copyOf(result);
    }

    private static Map<UUID, Instant> relationships(CreatorReportingQueryRepository.ReportingData data) {
        Map<UUID, Instant> result = new HashMap<>();
        data.orders().stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID
                || order.getStatus() == CommerceOrderStatus.REFUNDED).forEach(order -> {
            Instant timestamp = order.getPaidAt() == null ? order.getCreatedAt() : order.getPaidAt();
            result.merge(order.getBuyer().getUserId(), timestamp, CreatorDashboardService::earlier);
        });
        data.entitlements().forEach(entitlement -> result.merge(entitlement.getUser().getUserId(),
                entitlement.getCreatedAt(), CreatorDashboardService::earlier));
        return result;
    }

    private static long revenue(List<CommerceOrder> orders, ReportingWindow window, boolean previous) {
        return orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID)
                .filter(order -> previous ? window.previous(order.getPaidAt()) : window.current(order.getPaidAt()))
                .mapToLong(CommerceOrder::getTotalMinor).sum();
    }
    private static long paidOrders(List<CommerceOrder> orders, ReportingWindow window, boolean previous) {
        return orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID)
                .filter(order -> previous ? window.previous(order.getPaidAt()) : window.current(order.getPaidAt())).count();
    }
    private static Instant earlier(Instant a, Instant b) { return a.isBefore(b) ? a : b; }
    private static String relative(Instant timestamp, Instant now) {
        long minutes = Math.max(0, Duration.between(timestamp, now).toMinutes());
        if (minutes < 60) return Math.max(1, minutes) + " min ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        if (days < 7) return days + (days == 1 ? " day ago" : " days ago");
        return DATE.format(timestamp);
    }

    private record TimedActivity(Instant timestamp, CreatorDashboardDtos.Activity activity) {}
    private static final class ProductTotal {
        private final UUID id; private final String name; private final String type; private final String image; private long revenue;
        private ProductTotal(UUID id, String name, String type, String image) { this.id=id; this.name=name; this.type=type; this.image=image; }
        long revenue() { return revenue; }
    }
}

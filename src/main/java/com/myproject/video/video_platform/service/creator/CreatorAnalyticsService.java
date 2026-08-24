package com.myproject.video.video_platform.service.creator;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.dto.creator.analytics.CreatorAnalyticsDtos;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.commerce.CommerceOrderItem;
import com.myproject.video.video_platform.entity.entitlement.ProductEntitlement;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.repository.creator.CreatorReportingQueryRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatorAnalyticsService {
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    private final CurrentUserService currentUserService;
    private final CreatorReportingQueryRepository reportingRepository;
    private final Clock reportingClock;

    @Transactional(readOnly = true)
    public CreatorAnalyticsDtos.Overview overview(String periodValue) {
        ReportingEnums.Period period = ReportingEnums.Period.analytics(periodValue);
        ReportingWindow window = ReportingWindow.forPeriod(period, reportingClock);
        var data = reportingRepository.load(currentUserService.getCurrentUserId());
        String periodLabel = "last " + period.days() + " days";
        String previousLabel = "previous " + period.days() + " days";

        long revenue = paidRevenue(data.orders(), window, false);
        long previousRevenue = paidRevenue(data.orders(), window, true);
        long orders = paidOrders(data.orders(), window, false);
        long previousOrders = paidOrders(data.orders(), window, true);
        Map<UUID, Instant> firstRelationships = firstRelationships(data.orders(), data.entitlements());
        long totalCustomers = firstRelationships.size();
        long newCustomers = firstRelationships.values().stream().filter(window::current).count();
        long previousNewCustomers = firstRelationships.values().stream().filter(window::previous).count();
        String currency = data.orders().stream().map(CommerceOrder::getCurrency).findFirst().orElse("EUR");

        List<CreatorAnalyticsDtos.SeriesPoint> series = series(period, window, data.orders(), firstRelationships);
        var revenueComparison = CreatorReportingSupport.comparison(revenue, previousRevenue, false);
        var orderComparison = CreatorReportingSupport.comparison(orders, previousOrders, false);
        var customerComparison = CreatorReportingSupport.comparison(newCustomers, previousNewCustomers, false);
        List<CreatorAnalyticsDtos.Metric> metrics = List.of(
                metric("revenue", "Revenue", CreatorReportingSupport.money(revenue, currency), revenueComparison, previousLabel),
                metric("orders", "Orders", Long.toString(orders), orderComparison, previousLabel),
                metric("customers", "Customers", Long.toString(totalCustomers), customerComparison, previousLabel)
        );

        long refunds = eventCount(data.orders(), CommerceOrderStatus.REFUNDED, window, false);
        long previousRefunds = eventCount(data.orders(), CommerceOrderStatus.REFUNDED, window, true);
        long failures = eventCount(data.orders(), CommerceOrderStatus.FAILED, window, false);
        long previousFailures = eventCount(data.orders(), CommerceOrderStatus.FAILED, window, true);
        double refundRate = rate(refunds, orders + refunds);
        double previousRefundRate = rate(previousRefunds, previousOrders + previousRefunds);
        var refundComparison = decimalComparison(refundRate, previousRefundRate, true);
        var failureComparison = CreatorReportingSupport.comparison(failures, previousFailures, true);

        return new CreatorAnalyticsDtos.Overview(
                period.wireValue(), periodLabel, previousLabel, metrics,
                new CreatorAnalyticsDtos.Performance(series, delta(revenue, previousRevenue), delta(orders, previousOrders)),
                products(data, window, revenue),
                new CreatorAnalyticsDtos.CustomerGrowth(
                        new CreatorAnalyticsDtos.CustomerGrowthSummary(totalCustomers, newCustomers,
                                withPeriod(customerComparison.text(), previousLabel)), series),
                new CreatorAnalyticsDtos.Memberships(null, List.of()),
                new CreatorAnalyticsDtos.PaymentHealth(List.of(
                        new CreatorAnalyticsDtos.PaymentHealthMetric("refund-rate", "Refund rate",
                                formatPercent(refundRate), withPeriod(refundComparison.text(), previousLabel),
                                refundComparison.direction(), refundComparison.sentiment()),
                        new CreatorAnalyticsDtos.PaymentHealthMetric("failed-payments", "Failed payments",
                                Long.toString(failures), withPeriod(failureComparison.text(), previousLabel),
                                failureComparison.direction(), failureComparison.sentiment())
                ), series)
        );
    }

    private static CreatorAnalyticsDtos.Metric metric(
            String id, String label, String value, CreatorReportingSupport.Comparison comparison, String previousLabel
    ) {
        return new CreatorAnalyticsDtos.Metric(id, label, value, withPeriod(comparison.text(), previousLabel),
                comparison.direction(), comparison.sentiment());
    }

    private static List<CreatorAnalyticsDtos.SeriesPoint> series(
            ReportingEnums.Period period, ReportingWindow window, List<CommerceOrder> orders,
            Map<UUID, Instant> firstRelationships
    ) {
        List<Bucket> buckets = buckets(period, window);
        List<CreatorAnalyticsDtos.SeriesPoint> result = new ArrayList<>();
        for (Bucket bucket : buckets) {
            long revenue = orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID)
                    .filter(order -> bucket.contains(order.getPaidAt())).mapToLong(CommerceOrder::getTotalMinor).sum();
            long paidOrders = orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID)
                    .filter(order -> bucket.contains(order.getPaidAt())).count();
            long customers = firstRelationships.values().stream().filter(bucket::contains).count();
            long refunds = orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.REFUNDED)
                    .filter(order -> bucket.contains(order.getRefundedAt())).count();
            long failures = orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.FAILED)
                    .filter(order -> bucket.contains(order.getFailedAt())).count();
            result.add(new CreatorAnalyticsDtos.SeriesPoint(bucket.label(), revenue, paidOrders, customers,
                    0, 0, refunds, failures));
        }
        return List.copyOf(result);
    }

    private static List<Bucket> buckets(ReportingEnums.Period period, ReportingWindow window) {
        List<Bucket> buckets = new ArrayList<>();
        if (period != ReportingEnums.Period.DAYS_90) {
            for (int day = 0; day < period.days(); day++) {
                Instant start = window.start().plusSeconds(day * 86_400L);
                Instant end = start.plusSeconds(86_400L);
                buckets.add(new Bucket(DAY_LABEL.format(start), start, end));
            }
            return buckets;
        }
        Instant cursor = window.start();
        for (int index = 0; index < 13; index++) {
            int days = index < 12 ? 7 : 6;
            Instant end = cursor.plusSeconds(days * 86_400L);
            buckets.add(new Bucket("Week " + (index + 1), cursor, end));
            cursor = end;
        }
        return buckets;
    }

    private static List<CreatorAnalyticsDtos.Product> products(
            CreatorReportingQueryRepository.ReportingData data, ReportingWindow window, long totalRevenue
    ) {
        Map<UUID, ProductAggregate> aggregates = new LinkedHashMap<>();
        Map<UUID, Product> current = CreatorReportingSupport.productsById(data.products());
        data.orders().stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID)
                .filter(order -> window.current(order.getPaidAt())).forEach(order -> order.getItems().forEach(item -> {
                    ProductAggregate aggregate = aggregates.computeIfAbsent(item.getProductId(), ignored ->
                            new ProductAggregate(item.getProductId(), item.getProductName(),
                                    CreatorReportingSupport.productType(item.getProductType()),
                                    current.containsKey(item.getProductId()) ? current.get(item.getProductId()).getImage() : null));
                    aggregate.revenue += item.getLineTotalMinor();
                    aggregate.orderIds.add(order.getId());
                }));
        return aggregates.values().stream().sorted(Comparator.comparingLong(ProductAggregate::revenue).reversed())
                .map(product -> new CreatorAnalyticsDtos.Product(product.id.toString(), product.name, product.type,
                        product.revenue, product.orderIds.size(), totalRevenue == 0 ? 0.0
                        : BigDecimal.valueOf(product.revenue * 100.0 / totalRevenue).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                        product.thumbnailUrl)).toList();
    }

    private static Map<UUID, Instant> firstRelationships(
            List<CommerceOrder> orders, List<ProductEntitlement> entitlements
    ) {
        Map<UUID, Instant> first = new HashMap<>();
        orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID
                || order.getStatus() == CommerceOrderStatus.REFUNDED).forEach(order -> {
            Instant timestamp = order.getPaidAt() == null ? order.getCreatedAt() : order.getPaidAt();
            first.merge(order.getBuyer().getUserId(), timestamp, CreatorAnalyticsService::earlier);
        });
        entitlements.forEach(entitlement -> first.merge(entitlement.getUser().getUserId(),
                entitlement.getCreatedAt(), CreatorAnalyticsService::earlier));
        return first;
    }

    private static Instant earlier(Instant first, Instant second) { return first.isBefore(second) ? first : second; }
    private static long paidRevenue(List<CommerceOrder> orders, ReportingWindow window, boolean previous) {
        return orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID)
                .filter(order -> previous ? window.previous(order.getPaidAt()) : window.current(order.getPaidAt()))
                .mapToLong(CommerceOrder::getTotalMinor).sum();
    }
    private static long paidOrders(List<CommerceOrder> orders, ReportingWindow window, boolean previous) {
        return orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID)
                .filter(order -> previous ? window.previous(order.getPaidAt()) : window.current(order.getPaidAt())).count();
    }
    private static long eventCount(List<CommerceOrder> orders, CommerceOrderStatus status, ReportingWindow window, boolean previous) {
        return orders.stream().filter(order -> order.getStatus() == status).filter(order -> {
            Instant timestamp = status == CommerceOrderStatus.REFUNDED ? order.getRefundedAt() : order.getFailedAt();
            return previous ? window.previous(timestamp) : window.current(timestamp);
        }).count();
    }
    private static double delta(long current, long previous) {
        if (previous == 0) return current == 0 ? 0.0 : 100.0;
        return BigDecimal.valueOf((current - previous) * 100.0 / previous).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
    private static double rate(long numerator, long denominator) { return denominator == 0 ? 0.0 : numerator * 100.0 / denominator; }
    private static String formatPercent(double value) { return String.format(Locale.US, "%.1f%%", value); }
    private static CreatorReportingSupport.Comparison decimalComparison(double current, double previous, boolean lowerIsBetter) {
        return CreatorReportingSupport.comparison(Math.round(current * 10), Math.round(previous * 10), lowerIsBetter);
    }
    private static String withPeriod(String comparison, String previousLabel) {
        return "No change".equals(comparison) || "New this period".equals(comparison)
                ? comparison : comparison + " vs " + previousLabel;
    }

    private record Bucket(String label, Instant start, Instant end) {
        boolean contains(Instant value) { return value != null && !value.isBefore(start) && value.isBefore(end); }
    }
    private static final class ProductAggregate {
        private final UUID id;
        private final String name;
        private final String type;
        private final String thumbnailUrl;
        private long revenue;
        private final Set<UUID> orderIds = new HashSet<>();
        private ProductAggregate(UUID id, String name, String type, String thumbnailUrl) {
            this.id = id; this.name = name; this.type = type; this.thumbnailUrl = thumbnailUrl;
        }
        long revenue() { return revenue; }
    }
}

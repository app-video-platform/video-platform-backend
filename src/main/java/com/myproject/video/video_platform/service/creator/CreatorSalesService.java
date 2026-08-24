package com.myproject.video.video_platform.service.creator;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.dto.creator.CreatorPageResponse;
import com.myproject.video.video_platform.dto.creator.sales.CreatorSalesDtos;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.commerce.CommerceOrderItem;
import com.myproject.video.video_platform.entity.commerce.CommercePaymentAttempt;
import com.myproject.video.video_platform.entity.entitlement.ProductEntitlement;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.creator.CreatorReportingQueryRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatorSalesService {
    private final CurrentUserService currentUserService;
    private final CreatorReportingQueryRepository reportingRepository;
    private final Clock reportingClock;

    @Transactional(readOnly = true)
    public CreatorSalesDtos.Summary summary(String periodValue) {
        ReportingEnums.Period period = ReportingEnums.Period.sales(periodValue);
        ReportingWindow window = ReportingWindow.forPeriod(period, reportingClock);
        var data = reportingRepository.load(currentUserService.getCurrentUserId());
        long currentRevenue = sum(data.orders(), CommerceOrderStatus.PAID, window, Event.PAID);
        long previousRevenue = sumPrevious(data.orders(), CommerceOrderStatus.PAID, window, Event.PAID);
        long currentOrders = count(data.orders(), CommerceOrderStatus.PAID, window, Event.PAID, false);
        long previousOrders = count(data.orders(), CommerceOrderStatus.PAID, window, Event.PAID, true);
        long currentRefunds = sum(data.orders(), CommerceOrderStatus.REFUNDED, window, Event.REFUNDED);
        long previousRefunds = sumPrevious(data.orders(), CommerceOrderStatus.REFUNDED, window, Event.REFUNDED);
        long currentFailures = count(data.orders(), CommerceOrderStatus.FAILED, window, Event.FAILED, false);
        long previousFailures = count(data.orders(), CommerceOrderStatus.FAILED, window, Event.FAILED, true);
        String currency = data.orders().stream().map(CommerceOrder::getCurrency).findFirst().orElse("EUR");
        return new CreatorSalesDtos.Summary(period.wireValue(), List.of(
                metric("Revenue", CreatorReportingSupport.money(currentRevenue, currency), currentRevenue, previousRevenue, false),
                metric("Orders", Long.toString(currentOrders), currentOrders, previousOrders, false),
                metric("Refunds", CreatorReportingSupport.money(currentRefunds, currency), currentRefunds, previousRefunds, true),
                metric("Failed payments", Long.toString(currentFailures), currentFailures, previousFailures, true)
        ));
    }

    @Transactional(readOnly = true)
    public CreatorSalesDtos.OrdersPage orders(
            int page, int pageSize, String search, String statusValue, String productValue,
            String periodValue, String sortValue
    ) {
        validatePage(page, pageSize);
        ReportingEnums.Period period = ReportingEnums.Period.sales(periodValue);
        ReportingEnums.SalesStatus status = ReportingEnums.SalesStatus.parse(statusValue);
        ReportingEnums.SalesSort sort = ReportingEnums.SalesSort.parse(sortValue);
        UUID productId = productValue == null || productValue.isBlank() ? null : parseUuid(productValue, "product");
        ReportingWindow window = ReportingWindow.forPeriod(period, reportingClock);
        Instant now = reportingClock.instant();
        var data = reportingRepository.load(currentUserService.getCurrentUserId());
        Set<UUID> qualifyingCustomers = CreatorReportingSupport.qualifyingCustomerIds(data.orders(), data.entitlements());
        Map<UUID, Product> products = CreatorReportingSupport.productsById(data.products());
        Map<UUID, ProductEntitlement> access = CreatorReportingSupport.purchaseEntitlementsByItem(data.entitlements());
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        List<CommerceOrder> filtered = data.orders().stream()
                .filter(order -> visible(order, now))
                .filter(order -> window.current(order.getCreatedAt()))
                .filter(order -> status == ReportingEnums.SalesStatus.ALL || order.getStatus() == status.status())
                .filter(order -> productId == null || order.getItems().stream().anyMatch(item -> item.getProductId().equals(productId)))
                .filter(order -> matches(order, normalizedSearch))
                .sorted(orderComparator(sort))
                .toList();
        List<CreatorSalesDtos.OrderListItem> mapped = filtered.stream()
                .map(order -> listItem(order, qualifyingCustomers, products, access)).toList();
        CreatorPageResponse<CreatorSalesDtos.OrderListItem> result = CreatorPageResponse.of(mapped, page, pageSize);
        return new CreatorSalesDtos.OrdersPage(result.content(), result.totalElements(), result.totalPages(),
                result.size(), result.number(), result.first(), result.last(), result.empty(),
                CreatorReportingSupport.productOptions(data.products(), data.orders()));
    }

    @Transactional(readOnly = true)
    public CreatorSalesDtos.OrderDetail order(UUID orderId) {
        var data = reportingRepository.load(currentUserService.getCurrentUserId());
        CommerceOrder order = data.orders().stream().filter(candidate -> candidate.getId().equals(orderId))
                .filter(candidate -> visible(candidate, reportingClock.instant()))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        Set<UUID> qualifyingCustomers = CreatorReportingSupport.qualifyingCustomerIds(data.orders(), data.entitlements());
        Map<UUID, Product> products = CreatorReportingSupport.productsById(data.products());
        Map<UUID, ProductEntitlement> access = CreatorReportingSupport.purchaseEntitlementsByItem(data.entitlements());
        List<CreatorSalesDtos.Item> items = items(order, products, access);
        CommercePaymentAttempt attempt = data.attemptsByOrderId().get(order.getId());
        List<CreatorSalesDtos.SummaryRow> rows = new ArrayList<>();
        order.getItems().forEach(item -> rows.add(new CreatorSalesDtos.SummaryRow(item.getProductName(), item.getLineTotalMinor())));
        rows.add(new CreatorSalesDtos.SummaryRow("Total", order.getTotalMinor()));
        return new CreatorSalesDtos.OrderDetail(
                order.getId().toString(), order.getCreatedAt(), wireStatus(order), "one-time", order.getTotalMinor(),
                order.getCurrency(), customer(order, qualifyingCustomers), singleProduct(items), items,
                attempt == null ? null : title(attempt.getProvider().name()), null,
                attempt == null ? null : attempt.getProviderPaymentId(), order.getPaidAt(), rows,
                singleAccess(items), order.getStatus() == CommerceOrderStatus.REFUNDED
                        ? new CreatorSalesDtos.Refund(order.getTotalMinor(), order.getRefundedAt(), null) : null,
                order.getStatus() == CommerceOrderStatus.FAILED
                        ? new CreatorSalesDtos.Failure(attempt == null || attempt.getFailureMessage() == null
                        ? "Payment failed." : attempt.getFailureMessage(), null) : null
        );
    }

    private CreatorSalesDtos.OrderListItem listItem(
            CommerceOrder order, Set<UUID> qualifyingCustomers, Map<UUID, Product> products,
            Map<UUID, ProductEntitlement> access
    ) {
        List<CreatorSalesDtos.Item> items = items(order, products, access);
        return new CreatorSalesDtos.OrderListItem(order.getId().toString(), order.getCreatedAt(), wireStatus(order),
                "one-time", order.getTotalMinor(), order.getCurrency(), customer(order, qualifyingCustomers),
                singleProduct(items), items);
    }

    private List<CreatorSalesDtos.Item> items(
            CommerceOrder order, Map<UUID, Product> products, Map<UUID, ProductEntitlement> access
    ) {
        return order.getItems().stream().map(item -> new CreatorSalesDtos.Item(
                CreatorReportingSupport.salesProduct(item, products), item.getLineTotalMinor(),
                CreatorReportingSupport.access(access.get(item.getId())))).toList();
    }

    private CreatorSalesDtos.Customer customer(CommerceOrder order, Set<UUID> qualifying) {
        var buyer = order.getBuyer();
        return new CreatorSalesDtos.Customer(qualifying.contains(buyer.getUserId()) ? buyer.getUserId().toString() : null,
                CreatorReportingSupport.name(buyer), buyer.getEmail());
    }

    private static CreatorSalesDtos.Product singleProduct(List<CreatorSalesDtos.Item> items) {
        return items.size() == 1 ? items.get(0).product() : null;
    }

    private static CreatorSalesDtos.Access singleAccess(List<CreatorSalesDtos.Item> items) {
        return items.size() == 1 ? items.get(0).access() : null;
    }

    private static boolean visible(CommerceOrder order, Instant now) {
        if (order.getStatus() == CommerceOrderStatus.EXPIRED) return false;
        return order.getStatus() != CommerceOrderStatus.PENDING || order.getExpiresAt().isAfter(now);
    }

    private static boolean matches(CommerceOrder order, String search) {
        if (search.isBlank()) return true;
        String customer = (CreatorReportingSupport.name(order.getBuyer()) + " " + order.getBuyer().getEmail())
                .toLowerCase(Locale.ROOT);
        return order.getId().toString().toLowerCase(Locale.ROOT).contains(search) || customer.contains(search);
    }

    private static Comparator<CommerceOrder> orderComparator(ReportingEnums.SalesSort sort) {
        Comparator<CommerceOrder> comparator = switch (sort) {
            case NEWEST -> Comparator.comparing(CommerceOrder::getCreatedAt).reversed();
            case OLDEST -> Comparator.comparing(CommerceOrder::getCreatedAt);
            case AMOUNT_DESC -> Comparator.comparingLong(CommerceOrder::getTotalMinor).reversed();
            case AMOUNT_ASC -> Comparator.comparingLong(CommerceOrder::getTotalMinor);
        };
        return comparator.thenComparing(CommerceOrder::getId);
    }

    private static String wireStatus(CommerceOrder order) {
        return order.getStatus().name().toLowerCase(Locale.ROOT);
    }

    private static String title(String value) {
        return value.substring(0, 1) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private static CreatorSalesDtos.Metric metric(
            String label, String value, long current, long previous, boolean lowerIsBetter
    ) {
        var comparison = CreatorReportingSupport.comparison(current, previous, lowerIsBetter);
        return new CreatorSalesDtos.Metric(label, value, comparison.direction(), comparison.sentiment(), comparison.text());
    }

    private enum Event { PAID, FAILED, REFUNDED }
    private static Instant timestamp(CommerceOrder order, Event event) {
        return switch (event) { case PAID -> order.getPaidAt(); case FAILED -> order.getFailedAt(); case REFUNDED -> order.getRefundedAt(); };
    }
    private static long sum(List<CommerceOrder> orders, CommerceOrderStatus status, ReportingWindow window, Event event) {
        return orders.stream().filter(order -> order.getStatus() == status).filter(order -> window.current(timestamp(order, event)))
                .mapToLong(CommerceOrder::getTotalMinor).sum();
    }
    private static long sumPrevious(List<CommerceOrder> orders, CommerceOrderStatus status, ReportingWindow window, Event event) {
        return orders.stream().filter(order -> order.getStatus() == status).filter(order -> window.previous(timestamp(order, event)))
                .mapToLong(CommerceOrder::getTotalMinor).sum();
    }
    private static long count(List<CommerceOrder> orders, CommerceOrderStatus status, ReportingWindow window, Event event, boolean previous) {
        return orders.stream().filter(order -> order.getStatus() == status)
                .filter(order -> previous ? window.previous(timestamp(order, event)) : window.current(timestamp(order, event))).count();
    }
    private static UUID parseUuid(String value, String field) {
        try { return UUID.fromString(value); } catch (IllegalArgumentException ex) { throw new IllegalArgumentException(field + " must be a UUID"); }
    }
    private static void validatePage(int page, int pageSize) {
        if (page < 0) throw new IllegalArgumentException("page must be at least 0");
        if (pageSize < 1 || pageSize > 100) throw new IllegalArgumentException("pageSize must be between 1 and 100");
    }
}

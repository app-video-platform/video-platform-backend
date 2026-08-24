package com.myproject.video.video_platform.service.creator;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;
import com.myproject.video.video_platform.common.enums.entitlement.EntitlementSource;
import com.myproject.video.video_platform.common.enums.entitlement.EntitlementStatus;
import com.myproject.video.video_platform.dto.creator.CreatorPageResponse;
import com.myproject.video.video_platform.dto.creator.customers.CreatorCustomerDtos;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.commerce.CommerceOrderItem;
import com.myproject.video.video_platform.entity.entitlement.ProductEntitlement;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.repository.creator.CreatorReportingQueryRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreatorCustomersService {
    private static final DateTimeFormatter ACTIVITY_LABEL = DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.ENGLISH)
            .withZone(ZoneOffset.UTC);

    private final CurrentUserService currentUserService;
    private final CreatorReportingQueryRepository reportingRepository;

    @Transactional(readOnly = true)
    public CreatorCustomerDtos.Page customers(
            int page, int pageSize, String search, String statusValue, String productValue,
            String membershipValue, String sortValue
    ) {
        validatePage(page, pageSize);
        ReportingEnums.CustomerStatus status = ReportingEnums.CustomerStatus.parse(statusValue);
        ReportingEnums.MembershipStatus membership = ReportingEnums.MembershipStatus.parse(membershipValue);
        ReportingEnums.CustomerSort sort = ReportingEnums.CustomerSort.parse(sortValue);
        UUID productId = productValue == null || productValue.isBlank() ? null : parseUuid(productValue, "product");
        var data = reportingRepository.load(currentUserService.getCurrentUserId());
        Map<UUID, CustomerAggregate> aggregates = aggregate(data);
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        List<CreatorCustomerDtos.ListItem> all;
        if ((status != ReportingEnums.CustomerStatus.ALL && status != ReportingEnums.CustomerStatus.BUYER)
                || (membership != ReportingEnums.MembershipStatus.ALL && membership != ReportingEnums.MembershipStatus.NONE)) {
            all = List.of();
        } else {
            all = aggregates.values().stream()
                    .filter(customer -> normalizedSearch.isBlank() || customer.searchText().contains(normalizedSearch))
                    .filter(customer -> productId == null || customer.products().stream()
                            .anyMatch(product -> product.id().equals(productId.toString())))
                    .sorted(customerComparator(sort))
                    .map(CustomerAggregate::listItem)
                    .toList();
        }
        CreatorPageResponse<CreatorCustomerDtos.ListItem> result = CreatorPageResponse.of(all, page, pageSize);
        return new CreatorCustomerDtos.Page(result.content(), result.totalElements(), result.totalPages(), result.size(),
                result.number(), result.first(), result.last(), result.empty(), productOptions(data));
    }

    @Transactional(readOnly = true)
    public CreatorCustomerDtos.Detail customer(UUID customerId) {
        var data = reportingRepository.load(currentUserService.getCurrentUserId());
        CustomerAggregate aggregate = aggregate(data).get(customerId);
        if (aggregate == null) throw new ResourceNotFoundException("Customer not found");
        return aggregate.detail();
    }

    private Map<UUID, CustomerAggregate> aggregate(CreatorReportingQueryRepository.ReportingData data) {
        Set<UUID> qualifying = CreatorReportingSupport.qualifyingCustomerIds(data.orders(), data.entitlements());
        Map<UUID, Product> currentProducts = CreatorReportingSupport.productsById(data.products());
        Map<UUID, CreatorCustomerDtos.Product> productCatalog = productCatalog(data, currentProducts);
        Map<UUID, List<CommerceOrder>> ordersByCustomer = data.orders().stream()
                .collect(Collectors.groupingBy(order -> order.getBuyer().getUserId()));
        Map<UUID, List<ProductEntitlement>> accessByCustomer = data.entitlements().stream()
                .collect(Collectors.groupingBy(entitlement -> entitlement.getUser().getUserId()));
        Map<UUID, CustomerAggregate> result = new HashMap<>();
        for (UUID customerId : qualifying) {
            List<CommerceOrder> customerOrders = ordersByCustomer.getOrDefault(customerId, List.of());
            List<ProductEntitlement> customerAccess = accessByCustomer.getOrDefault(customerId, List.of());
            User user = !customerOrders.isEmpty() ? customerOrders.get(0).getBuyer() : customerAccess.get(0).getUser();
            result.put(customerId, build(user, customerOrders, customerAccess, productCatalog));
        }
        return result;
    }

    private CustomerAggregate build(
            User user, List<CommerceOrder> orders, List<ProductEntitlement> entitlements,
            Map<UUID, CreatorCustomerDtos.Product> catalog
    ) {
        List<CommerceOrder> completed = orders.stream()
                .filter(order -> order.getStatus() == CommerceOrderStatus.PAID || order.getStatus() == CommerceOrderStatus.REFUNDED)
                .toList();
        long spend = orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID)
                .mapToLong(CommerceOrder::getTotalMinor).sum();
        Map<UUID, CreatorCustomerDtos.Product> relatedProducts = new LinkedHashMap<>();
        completed.stream().flatMap(order -> order.getItems().stream()).forEach(item ->
                relatedProducts.putIfAbsent(item.getProductId(), catalog.get(item.getProductId())));
        entitlements.forEach(entitlement -> relatedProducts.putIfAbsent(
                entitlement.getProductId(), catalog.get(entitlement.getProductId())));
        relatedProducts.values().removeIf(value -> value == null);

        List<CreatorCustomerDtos.Purchase> purchases = orders.stream()
                .filter(order -> order.getStatus() == CommerceOrderStatus.PAID
                        || order.getStatus() == CommerceOrderStatus.REFUNDED
                        || order.getStatus() == CommerceOrderStatus.FAILED)
                .flatMap(order -> order.getItems().stream().map(item -> purchase(order, item)))
                .sorted(Comparator.comparing(CreatorCustomerDtos.Purchase::purchasedAt).reversed())
                .toList();
        List<CreatorCustomerDtos.Access> access = entitlements.stream().map(entitlement ->
                access(entitlement, catalog.get(entitlement.getProductId())))
                .sorted(Comparator.comparing(CreatorCustomerDtos.Access::grantedAt).reversed()).toList();
        List<CreatorCustomerDtos.Activity> activity = activity(orders, entitlements, catalog);
        Instant lastActivity = activity.isEmpty() ? null : activity.get(0).occurredAt();
        Instant customerSince = StreamSupport.min(
                completed.stream().map(order -> order.getPaidAt() == null ? order.getCreatedAt() : order.getPaidAt()).toList(),
                entitlements.stream().map(ProductEntitlement::getCreatedAt).toList());
        String location = joinLocation(user.getCity(), user.getCountry());
        List<CreatorCustomerDtos.Product> products = relatedProducts.values().stream()
                .sorted(Comparator.comparing(CreatorCustomerDtos.Product::name, String.CASE_INSENSITIVE_ORDER)).toList();
        CreatorCustomerDtos.ListItem listItem = new CreatorCustomerDtos.ListItem(
                user.getUserId().toString(), CreatorReportingSupport.name(user), user.getEmail(), null,
                "buyer", "none", products, spend, completed.size(),
                entitlements.stream().filter(entitlement -> entitlement.getStatus() == EntitlementStatus.ACTIVE).count(),
                lastActivity, lastActivity == null ? null : ACTIVITY_LABEL.format(lastActivity));
        CreatorCustomerDtos.Detail detail = new CreatorCustomerDtos.Detail(
                listItem.id(), listItem.name(), listItem.email(), null, listItem.relationshipStatus(),
                listItem.membershipState(), products, listItem.totalSpendCents(), listItem.ordersCount(),
                listItem.activeAccessCount(), lastActivity, listItem.lastActivityLabel(), null, location, null, null,
                customerSince, List.of(), List.of(), activity, purchases, access);
        return new CustomerAggregate(listItem, detail);
    }

    private static CreatorCustomerDtos.Purchase purchase(CommerceOrder order, CommerceOrderItem item) {
        String status = order.getStatus().name().toLowerCase(Locale.ROOT);
        Instant occurredAt = switch (order.getStatus()) {
            case PAID -> order.getPaidAt();
            case REFUNDED -> order.getPaidAt();
            case FAILED -> order.getFailedAt();
            default -> order.getCreatedAt();
        };
        if (occurredAt == null) occurredAt = order.getCreatedAt();
        return new CreatorCustomerDtos.Purchase(item.getId().toString(), item.getProductName(),
                CreatorReportingSupport.productType(item.getProductType()), occurredAt,
                item.getLineTotalMinor(), "One-time", status);
    }

    private static CreatorCustomerDtos.Access access(
            ProductEntitlement entitlement, CreatorCustomerDtos.Product product
    ) {
        String source = switch (entitlement.getSource()) {
            case PURCHASE -> "purchased";
            case FREE_ENROLLMENT -> "free";
            case ADMIN_GRANT -> "manual";
        };
        String name = product == null ? "Unavailable Product" : product.name();
        String type = product == null ? CreatorReportingSupport.productType(entitlement.getProductType()) : product.type();
        return new CreatorCustomerDtos.Access(entitlement.getId().toString(), name, type,
                entitlement.getStatus() == EntitlementStatus.ACTIVE ? "active" : "revoked", source,
                entitlement.getCreatedAt(), null);
    }

    private static List<CreatorCustomerDtos.Activity> activity(
            List<CommerceOrder> orders, List<ProductEntitlement> entitlements,
            Map<UUID, CreatorCustomerDtos.Product> catalog
    ) {
        List<CreatorCustomerDtos.Activity> result = new ArrayList<>();
        orders.stream().filter(order -> order.getStatus() == CommerceOrderStatus.PAID
                || order.getStatus() == CommerceOrderStatus.REFUNDED
                || order.getStatus() == CommerceOrderStatus.FAILED).forEach(order -> {
            String context = order.getItems().stream().map(CommerceOrderItem::getProductName).collect(Collectors.joining(", "));
            if (order.getStatus() == CommerceOrderStatus.PAID || order.getStatus() == CommerceOrderStatus.REFUNDED) {
                Instant paidAt = order.getPaidAt() == null ? order.getCreatedAt() : order.getPaidAt();
                result.add(new CreatorCustomerDtos.Activity("order-" + order.getId() + "-paid",
                        "Purchase completed", context, paidAt, null));
            }
            if (order.getStatus() == CommerceOrderStatus.REFUNDED) {
                Instant refundedAt = order.getRefundedAt() == null ? order.getCreatedAt() : order.getRefundedAt();
                result.add(new CreatorCustomerDtos.Activity("order-" + order.getId() + "-refunded",
                        "Order refunded", context, refundedAt, null));
            }
            if (order.getStatus() == CommerceOrderStatus.FAILED) {
                Instant failedAt = order.getFailedAt() == null ? order.getCreatedAt() : order.getFailedAt();
                result.add(new CreatorCustomerDtos.Activity("order-" + order.getId() + "-failed",
                        "Payment failed", context, failedAt, null));
            }
        });
        entitlements.forEach(entitlement -> {
            CreatorCustomerDtos.Product product = catalog.get(entitlement.getProductId());
            String context = product == null ? "Unavailable Product" : product.name();
            result.add(new CreatorCustomerDtos.Activity("access-grant-" + entitlement.getId(), "Access granted", context,
                    entitlement.getCreatedAt(), null));
            if (entitlement.getRevokedAt() != null) result.add(new CreatorCustomerDtos.Activity(
                    "access-revoke-" + entitlement.getId(), "Access revoked", context, entitlement.getRevokedAt(), null));
        });
        return result.stream().sorted(Comparator.comparing(CreatorCustomerDtos.Activity::occurredAt).reversed())
                .limit(20).toList();
    }

    private static Map<UUID, CreatorCustomerDtos.Product> productCatalog(
            CreatorReportingQueryRepository.ReportingData data, Map<UUID, Product> products
    ) {
        Map<UUID, CreatorCustomerDtos.Product> result = new HashMap<>();
        products.values().forEach(product -> result.put(product.getId(), new CreatorCustomerDtos.Product(
                product.getId().toString(), product.getName(), CreatorReportingSupport.productType(product.getType()))));
        data.orders().stream().flatMap(order -> order.getItems().stream()).forEach(item -> result.putIfAbsent(
                item.getProductId(), new CreatorCustomerDtos.Product(item.getProductId().toString(), item.getProductName(),
                        CreatorReportingSupport.productType(item.getProductType()))));
        return result;
    }

    private static List<CreatorCustomerDtos.Product> productOptions(CreatorReportingQueryRepository.ReportingData data) {
        return productCatalog(data, CreatorReportingSupport.productsById(data.products())).values().stream()
                .sorted(Comparator.comparing(CreatorCustomerDtos.Product::name, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    private static Comparator<CustomerAggregate> customerComparator(ReportingEnums.CustomerSort sort) {
        Comparator<CustomerAggregate> comparator = switch (sort) {
            case LAST_ACTIVITY_DESC -> Comparator.comparing(CustomerAggregate::lastActivity,
                    Comparator.nullsLast(Comparator.reverseOrder()));
            case SPEND_DESC -> Comparator.comparingLong(CustomerAggregate::spend).reversed();
            case SPEND_ASC -> Comparator.comparingLong(CustomerAggregate::spend);
            case NAME_ASC -> Comparator.comparing(CustomerAggregate::name, String.CASE_INSENSITIVE_ORDER);
            case NAME_DESC -> Comparator.comparing(CustomerAggregate::name, String.CASE_INSENSITIVE_ORDER).reversed();
        };
        return comparator.thenComparing(CustomerAggregate::id);
    }

    private record CustomerAggregate(CreatorCustomerDtos.ListItem listItem, CreatorCustomerDtos.Detail detail) {
        UUID id() { return UUID.fromString(listItem.id()); }
        String name() { return listItem.name(); }
        long spend() { return listItem.totalSpendCents(); }
        Instant lastActivity() { return listItem.lastActivityAt(); }
        String searchText() { return (listItem.name() + " " + listItem.email()).toLowerCase(Locale.ROOT); }
        List<CreatorCustomerDtos.Product> products() { return listItem.products(); }
    }

    private static final class StreamSupport {
        private StreamSupport() {}
        static Instant min(List<Instant> first, List<Instant> second) {
            return java.util.stream.Stream.concat(first.stream(), second.stream()).filter(value -> value != null)
                    .min(Comparator.naturalOrder()).orElse(null);
        }
    }

    private static String joinLocation(String city, String country) {
        if (city == null || city.isBlank()) return country == null || country.isBlank() ? null : country;
        if (country == null || country.isBlank()) return city;
        return city + ", " + country;
    }
    private static UUID parseUuid(String value, String field) {
        try { return UUID.fromString(value); } catch (IllegalArgumentException ex) { throw new IllegalArgumentException(field + " must be a UUID"); }
    }
    private static void validatePage(int page, int pageSize) {
        if (page < 0) throw new IllegalArgumentException("page must be at least 0");
        if (pageSize < 1 || pageSize > 100) throw new IllegalArgumentException("pageSize must be between 1 and 100");
    }
}

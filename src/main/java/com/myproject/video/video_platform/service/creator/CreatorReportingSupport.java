package com.myproject.video.video_platform.service.creator;

import com.myproject.video.video_platform.common.enums.entitlement.EntitlementStatus;
import com.myproject.video.video_platform.common.enums.products.ProductType;
import com.myproject.video.video_platform.dto.creator.sales.CreatorSalesDtos;
import com.myproject.video.video_platform.entity.commerce.CommerceOrder;
import com.myproject.video.video_platform.entity.commerce.CommerceOrderItem;
import com.myproject.video.video_platform.entity.entitlement.ProductEntitlement;
import com.myproject.video.video_platform.entity.products.Product;
import com.myproject.video.video_platform.entity.user.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CreatorReportingSupport {
    private CreatorReportingSupport() {}

    public static String productType(ProductType type) {
        return switch (type) {
            case COURSE -> "Course";
            case DOWNLOAD -> "Download";
            case CONSULTATION -> "Consultation";
        };
    }

    public static String name(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }

    public static Set<UUID> qualifyingCustomerIds(
            List<CommerceOrder> orders,
            List<ProductEntitlement> entitlements
    ) {
        Set<UUID> ids = orders.stream()
                .filter(order -> switch (order.getStatus()) {
                    case PAID, REFUNDED -> true;
                    default -> false;
                })
                .map(order -> order.getBuyer().getUserId())
                .collect(Collectors.toSet());
        entitlements.stream().map(entitlement -> entitlement.getUser().getUserId()).forEach(ids::add);
        return ids;
    }

    public static Map<UUID, Product> productsById(List<Product> products) {
        return products.stream().collect(Collectors.toMap(Product::getId, product -> product));
    }

    public static Map<UUID, ProductEntitlement> purchaseEntitlementsByItem(
            List<ProductEntitlement> entitlements
    ) {
        return entitlements.stream()
                .filter(entitlement -> entitlement.getPurchaseOrderItemId() != null)
                .collect(Collectors.toMap(ProductEntitlement::getPurchaseOrderItemId, entitlement -> entitlement));
    }

    public static CreatorSalesDtos.Product salesProduct(CommerceOrderItem item, Map<UUID, Product> products) {
        Product current = products.get(item.getProductId());
        return new CreatorSalesDtos.Product(
                item.getProductId().toString(), item.getProductName(), productType(item.getProductType()),
                current == null ? null : current.getImage()
        );
    }

    public static CreatorSalesDtos.Access access(ProductEntitlement entitlement) {
        if (entitlement == null) return new CreatorSalesDtos.Access("none", "No access granted", null);
        if (entitlement.getStatus() == EntitlementStatus.ACTIVE) {
            return new CreatorSalesDtos.Access("granted", "Access granted", null);
        }
        return new CreatorSalesDtos.Access("revoked", "Access revoked", null);
    }

    public static List<CreatorSalesDtos.Product> productOptions(
            List<Product> products,
            List<CommerceOrder> orders
    ) {
        Map<UUID, CreatorSalesDtos.Product> result = new LinkedHashMap<>();
        products.stream().sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER)).forEach(product ->
                result.put(product.getId(), new CreatorSalesDtos.Product(
                        product.getId().toString(), product.getName(), productType(product.getType()), product.getImage())));
        orders.stream().flatMap(order -> order.getItems().stream()).forEach(item -> result.putIfAbsent(
                item.getProductId(), new CreatorSalesDtos.Product(item.getProductId().toString(), item.getProductName(),
                        productType(item.getProductType()), null)));
        return List.copyOf(result.values());
    }

    public static String money(long cents, String currency) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(cents % 100 == 0 ? 0 : 2);
        format.setMaximumFractionDigits(2);
        return currency + " " + format.format(BigDecimal.valueOf(cents, 2));
    }

    public static Comparison comparison(long current, long previous, boolean lowerIsBetter) {
        if (current == previous) return new Comparison("flat", "neutral", "No change");
        String direction = current > previous ? "up" : "down";
        String sentiment = (current > previous) == lowerIsBetter ? "unfavorable" : "favorable";
        if (previous == 0) return new Comparison(direction, sentiment, "New this period");
        BigDecimal percent = BigDecimal.valueOf(current - previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 1, RoundingMode.HALF_UP);
        String prefix = percent.signum() > 0 ? "+" : "";
        return new Comparison(direction, sentiment, prefix + percent.stripTrailingZeros().toPlainString() + "%");
    }

    public static Instant latest(Collection<Instant> instants) {
        return instants.stream().filter(value -> value != null).max(Comparator.naturalOrder()).orElse(null);
    }

    public record Comparison(String direction, String sentiment, String text) {}
}

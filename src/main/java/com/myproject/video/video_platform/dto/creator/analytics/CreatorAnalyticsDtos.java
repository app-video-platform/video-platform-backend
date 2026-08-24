package com.myproject.video.video_platform.dto.creator.analytics;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreatorAnalyticsDtos {
    private CreatorAnalyticsDtos() {}

    public record Metric(String id, String label, String value, String comparison, String direction, String sentiment) {}
    public record SeriesPoint(
            String label, long revenue, long orders, long customers, long newMemberships,
            long cancelledMemberships, long refunds, long failedPayments
    ) {}
    public record Performance(List<SeriesPoint> series, double revenueDelta, double orderDelta) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Product(
            String id, String name, String type, long revenueCents, long orders, double share, String thumbnailUrl
    ) {}
    public record CustomerGrowthSummary(long totalCustomers, long newCustomers, String comparison) {}
    public record CustomerGrowth(CustomerGrowthSummary summary, List<SeriesPoint> series) {}
    public record Memberships(Object summary, List<SeriesPoint> series) {}
    public record PaymentHealthMetric(String id, String label, String value, String comparison, String direction, String sentiment) {}
    public record PaymentHealth(List<PaymentHealthMetric> metrics, List<SeriesPoint> series) {}
    public record Overview(
            String period,
            String periodLabel,
            String previousPeriodLabel,
            List<Metric> metrics,
            Performance performance,
            List<Product> products,
            CustomerGrowth customerGrowth,
            Memberships memberships,
            PaymentHealth paymentHealth
    ) {}
}

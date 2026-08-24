package com.myproject.video.video_platform.dto.creator.sales;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreatorSalesDtos {
    private CreatorSalesDtos() {
    }

    public record Metric(String label, String value, String direction, String sentiment, String comparison) {}
    public record Summary(String period, List<Metric> metrics) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Customer(String id, String name, String email) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Product(String id, String name, String type, String thumbnailUrl) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Access(String state, String label, String detail) {}
    public record Item(Product product, long amountCents, Access access) {}
    public record SummaryRow(String label, long amountCents) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Refund(long amountCents, Instant refundedAt, String reason) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Failure(String message, Instant retryAt) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderListItem(
            String id,
            Instant orderedAt,
            String status,
            String type,
            long amountCents,
            String currency,
            Customer customer,
            Product product,
            List<Item> items
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record OrderDetail(
            String id,
            Instant orderedAt,
            String status,
            String type,
            long amountCents,
            String currency,
            Customer customer,
            Product product,
            List<Item> items,
            String provider,
            String paymentMethod,
            String transactionId,
            Instant paymentDate,
            List<SummaryRow> summaryRows,
            Access access,
            Refund refund,
            Failure failure
    ) {}

    public record OrdersPage(
            List<OrderListItem> content,
            long totalElements,
            int totalPages,
            int size,
            int number,
            boolean first,
            boolean last,
            boolean empty,
            List<Product> productOptions
    ) {}
}

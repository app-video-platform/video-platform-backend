package com.myproject.video.video_platform.dto.creator.customers;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreatorCustomerDtos {
    private CreatorCustomerDtos() {}

    public record Product(String id, String name, String type) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ListItem(
            String id,
            String name,
            String email,
            String avatarUrl,
            String relationshipStatus,
            String membershipState,
            List<Product> products,
            long totalSpendCents,
            long ordersCount,
            long activeAccessCount,
            Instant lastActivityAt,
            String lastActivityLabel
    ) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Activity(String id, String label, String context, Instant occurredAt, String destinationPath) {}
    public record Purchase(
            String id, String productName, String productType, Instant purchasedAt,
            long amountCents, String paymentModel, String status
    ) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Access(
            String id, String productName, String productType, String status,
            String source, Instant grantedAt, Instant expiresAt
    ) {}
    public record Note(String id, String body, String author, Instant createdAt) {}
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Detail(
            String id,
            String name,
            String email,
            String avatarUrl,
            String relationshipStatus,
            String membershipState,
            List<Product> products,
            long totalSpendCents,
            long ordersCount,
            long activeAccessCount,
            Instant lastActivityAt,
            String lastActivityLabel,
            String phone,
            String location,
            String language,
            String timezone,
            Instant customerSince,
            List<String> tags,
            List<Note> notes,
            List<Activity> activity,
            List<Purchase> purchases,
            List<Access> access
    ) {}
    public record Page(
            List<ListItem> content,
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

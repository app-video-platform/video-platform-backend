package com.myproject.video.video_platform.dto.products.membership;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class MembershipDtos {
    private MembershipDtos() {
    }

    public record Config(UUID productId, String orderingMode) {
    }

    public record Aggregate(
            UUID productId,
            Config config,
            List<Content> content,
            List<FeedEntry> feed,
            LocalDateTime updatedAt
    ) {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Server-owned Membership content metadata; binary files are not uploaded by this API.")
    public static class Content {
        private UUID id;
        private String type;
        private String title;
        private String description;
        private String status;
        private String body;
        private AssetRef video;
        private AssetRef file;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer position;
    }

    public record AssetRef(UUID fileId, String fileName, String fileType, long size) {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FeedEntry {
        private String entryId;
        private String kind;
        private UUID contentId;
        private UUID productId;
        private Instant addedAt;
        private Integer position;
    }
}

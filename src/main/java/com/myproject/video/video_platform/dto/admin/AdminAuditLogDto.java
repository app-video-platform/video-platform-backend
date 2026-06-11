package com.myproject.video.video_platform.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AdminAuditLogDto {
    private Long id;
    private String actorUserId;
    private String action;
    private String targetType;
    private String targetId;
    private String beforeSummary;
    private String afterSummary;
    private Instant createdAt;
}

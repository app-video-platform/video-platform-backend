package com.myproject.video.video_platform.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialMediaLinkResponse {
    private UUID id;
    private SocialPlatform platform;
    private String url;
    private Instant createdAt;
}

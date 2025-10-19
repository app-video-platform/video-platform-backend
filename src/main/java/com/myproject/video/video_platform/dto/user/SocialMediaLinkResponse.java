package com.myproject.video.video_platform.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Published social media link shown on the teacher profile.")
public class SocialMediaLinkResponse {
    @Schema(description = "Link identifier", example = "fbe2f742-a1a2-4d4d-a0a1-7a8a91e9a925")
    private UUID id;
    @Schema(description = "Social platform", example = "INSTAGRAM")
    private SocialPlatform platform;
    @Schema(description = "Profile URL", example = "https://instagram.com/ameliahughes.studio")
    private String url;
    @Schema(description = "Creation timestamp", example = "2024-01-15T09:32:11Z")
    private Instant createdAt;
}

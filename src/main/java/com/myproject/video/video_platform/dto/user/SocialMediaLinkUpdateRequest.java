package com.myproject.video.video_platform.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Social media link mutation payload.")
public class SocialMediaLinkUpdateRequest {
    /**
     * If null → create new link.
     * Otherwise update the existing one (and any missing existing ones will be removed).
     */
    @Schema(description = "Existing link identifier", example = "fbe2f742-a1a2-4d4d-a0a1-7a8a91e9a925")
    private UUID id;

    @NotNull
    @Schema(description = "Platform enum", example = "INSTAGRAM")
    private SocialPlatform platform;

    @NotNull
    @Size(min = 5, max = 255)
    @Schema(description = "Profile URL", example = "https://instagram.com/ameliahughes.studio")
    private String url;
}

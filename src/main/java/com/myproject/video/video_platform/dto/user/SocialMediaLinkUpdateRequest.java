package com.myproject.video.video_platform.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialMediaLinkUpdateRequest {
    /**
     * If null → create new link.
     * Otherwise update the existing one (and any missing existing ones will be removed).
     */
    private UUID id;

    @NotNull
    private SocialPlatform platform;

    @NotNull
    @Size(min = 5, max = 255)
    private String url;
}

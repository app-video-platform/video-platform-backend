package com.myproject.video.video_platform.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supported social platforms that can be surfaced on a profile.")
public enum SocialPlatform {
    FACEBOOK,
    INSTAGRAM,
    X,
    TIKTOK,
    YOUTUBE
}

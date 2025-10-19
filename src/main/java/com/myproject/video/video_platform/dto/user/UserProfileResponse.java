package com.myproject.video.video_platform.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Public-facing profile surface returned by read endpoints.")
public class UserProfileResponse {
    @Schema(description = "User identifier", example = "738297f1-45fb-4f5f-98a5-6d0eb0a8f542")
    private UUID userId;
    @Schema(description = "Public title", example = "Lifestyle Photographer")
    private String title;
    @Schema(description = "Short biography", example = "Helping creatives build their first paid lifestyle shoots.")
    private String bio;
    @Schema(description = "Mission tagline", example = "Teach lifestyle photographers how to lead confident shoots")
    private String taglineMission;
    @Schema(description = "Personal website", example = "https://ameliahughes.studio")
    private String website;
    @Schema(description = "City", example = "Berlin")
    private String city;
    @Schema(description = "Country", example = "Germany")
    private String country;
    @Schema(description = "Published social links")
    private List<SocialMediaLinkResponse> socialLinks;
}

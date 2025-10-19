package com.myproject.video.video_platform.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Builder
@Data
@Schema(description = "Authenticated user profile returned after login or updates.")
public class UserDto {
    @Schema(description = "User identifier", example = "738297f1-45fb-4f5f-98a5-6d0eb0a8f542")
    private String id;
    @Schema(description = "First name", example = "Amelia")
    private String firstName;
    @Schema(description = "Last name", example = "Hughes")
    private String lastName;
    @Schema(description = "Login email", example = "amelia.hughes@example.com")
    private String email;
    @Schema(description = "Public title shown on storefront", example = "Lifestyle Photographer")
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
    @Schema(description = "Whether onboarding tour is done", example = "true")
    private boolean onboardingCompleted;
    @Schema(description = "Granted roles", example = "[\"ROLE_USER\"]")
    private List<String> roles;
    @Schema(description = "Published social links")
    private List<SocialMediaLinkResponse> socialLinks;
    @Schema(description = "Account creation timestamp", example = "2024-01-15T09:32:11Z")
    private Instant createdAt;
}

package com.myproject.video.video_platform.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Builder
@Data
public class UserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String title;
    private String bio;
    private String taglineMission;
    private String website;
    private String city;
    private String country;
    private boolean onboardingCompleted;
    private List<String> roles;
    private List<SocialMediaLinkResponse> socialLinks;
    private Instant createdAt;
}

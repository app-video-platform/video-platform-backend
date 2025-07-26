package com.myproject.video.video_platform.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private UUID userId;
    private String title;
    private String bio;
    private String taglineMission;
    private String website;
    private String city;
    private String country;
    private List<SocialMediaLinkResponse> socialLinks;
}

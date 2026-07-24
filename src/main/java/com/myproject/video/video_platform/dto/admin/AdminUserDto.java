package com.myproject.video.video_platform.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AdminUserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
    private boolean enabled;
    private String authProvider;
    private boolean onboardingCompleted;
    private Instant createdAt;
}

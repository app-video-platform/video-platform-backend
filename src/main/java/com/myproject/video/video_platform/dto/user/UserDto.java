package com.myproject.video.video_platform.dto.user;

import lombok.Builder;

import java.util.List;

@Builder
public class UserDto {
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
}

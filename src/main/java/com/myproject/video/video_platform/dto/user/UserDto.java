package com.myproject.video.video_platform.dto.user;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class UserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
}

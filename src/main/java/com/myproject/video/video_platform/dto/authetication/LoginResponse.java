package com.myproject.video.video_platform.dto.authetication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String firstName;
    private String lastName;
    private String email;
    private List<String> role;
    private String token;
}

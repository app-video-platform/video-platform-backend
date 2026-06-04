package com.myproject.video.video_platform.dto.user;

import com.myproject.video.video_platform.common.enums.user.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DevRoleChangeRequest {

    @NotNull
    private UserRole role;
}

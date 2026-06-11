package com.myproject.video.video_platform.dto.admin;

import com.myproject.video.video_platform.common.enums.user.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminRoleUpdateRequest {
    @NotNull
    private UserRole role;
}

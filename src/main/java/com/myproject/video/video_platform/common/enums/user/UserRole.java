package com.myproject.video.video_platform.common.enums.user;

public enum UserRole {
    ADMIN,
    CREATOR,
    USER;

    public String authority() {
        return "ROLE_" + name();
    }
}

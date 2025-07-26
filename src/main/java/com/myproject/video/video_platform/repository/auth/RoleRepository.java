package com.myproject.video.video_platform.repository.auth;

import com.myproject.video.video_platform.entity.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Role entity.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(String roleName);
}

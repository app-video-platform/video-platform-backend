package com.myproject.video.video_platform.repository;

import com.myproject.video.video_platform.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Role entity.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(String roleName);
}

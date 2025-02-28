package com.myproject.video.video_platform.repository;

import com.myproject.video.video_platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}

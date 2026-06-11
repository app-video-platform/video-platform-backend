package com.myproject.video.video_platform.repository.auth;

import com.myproject.video.video_platform.entity.user.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity.
 */
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(UUID userId);

    @Query("""
            SELECT COUNT(u)
              FROM User u
              JOIN u.roles r
             WHERE r.roleName = :roleName
            """)
    long countByRoleName(@Param("roleName") String roleName);
}

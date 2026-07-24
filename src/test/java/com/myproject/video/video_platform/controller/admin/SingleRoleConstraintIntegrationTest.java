package com.myproject.video.video_platform.controller.admin;

import com.myproject.video.video_platform.common.enums.user.UserRole;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class SingleRoleConstraintIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User user;
    private Role creatorRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        Role userRole = roleRepository.save(new Role(null, UserRole.USER.name()));
        creatorRole = roleRepository.save(new Role(null, UserRole.CREATOR.name()));

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("single-role@example.com");
        user.setPassword("password");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setEnabled(true);
        user.setRoles(Set.of(userRole));
        user = userRepository.saveAndFlush(user);
    }

    @Test
    void databaseRejectsASecondRoleForTheSameUser() {
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update(
                        "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                        user.getUserId(),
                        creatorRole.getRoleId()
                )
        );
    }
}

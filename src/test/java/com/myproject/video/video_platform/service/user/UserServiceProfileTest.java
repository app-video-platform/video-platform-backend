package com.myproject.video.video_platform.service.user;

import com.myproject.video.video_platform.dto.user.UpdateUserRequest;
import com.myproject.video.video_platform.entity.user.Role;
import com.myproject.video.video_platform.entity.user.SocialMediaLink;
import com.myproject.video.video_platform.entity.user.User;
import com.myproject.video.video_platform.repository.auth.RoleRepository;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceProfileTest {
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock CurrentUserService currentUserService;

    @Test
    void updateUsesAuthenticatedUserAndLeavesOmittedSocialLinksUntouched() {
        UUID authenticatedId = UUID.randomUUID();
        User user = new User();
        user.setUserId(authenticatedId); user.setFirstName("Before"); user.setLastName("Name");
        user.setEmail("private@example.test"); user.setRoles(Set.of(new Role(1L, "CREATOR")));
        SocialMediaLink link = new SocialMediaLink(); link.setId(UUID.randomUUID());
        user.setSocialLinks(new ArrayList<>()); user.addSocialLink(link);
        when(currentUserService.getCurrentUserId()).thenReturn(authenticatedId);
        when(userRepository.findById(authenticatedId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUserId(UUID.randomUUID().toString());
        request.setFirstName(" Updated ");
        request.setPublicEmail(" public@example.test ");

        var result = new UserService(userRepository, roleRepository, currentUserService, false).updateUserInfo(request);

        assertEquals("Updated", result.getFirstName());
        assertEquals("public@example.test", result.getPublicEmail());
        assertEquals(1, result.getSocialLinks().size());
    }
}

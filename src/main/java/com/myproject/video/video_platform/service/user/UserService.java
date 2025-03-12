package com.myproject.video.video_platform.service.user;

import com.myproject.video.video_platform.dto.user.UserDto;
import com.myproject.video.video_platform.entity.auth.Role;
import com.myproject.video.video_platform.entity.auth.User;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import com.myproject.video.video_platform.repository.auth.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public UserDto getUserInfo(Authentication authentication) {
        // Extract the user’s principal (email) from the Authentication object.
        String email = (String) authentication.getPrincipal();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        return UserDto
                .builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(Role::getRoleName).toList())
                .build();
    }
}

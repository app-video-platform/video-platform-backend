package com.myproject.video.video_platform.controller.user;

import com.myproject.video.video_platform.dto.user.UserDto;
import com.myproject.video.video_platform.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling user account actions.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to retrieve the authenticated user's basic info.
     * Requires a valid JWT, so Spring Security sets Authentication.
     */
    @GetMapping("/userInfo")
    public ResponseEntity<UserDto> getUserInfo(Authentication authentication) {
        UserDto userDto = userService.getUserInfo(authentication);
        return ResponseEntity.ok(userDto);
    }
}

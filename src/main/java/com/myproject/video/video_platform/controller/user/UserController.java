package com.myproject.video.video_platform.controller.user;

import com.myproject.video.video_platform.controller.docs.user.UserApiDoc;
import com.myproject.video.video_platform.dto.user.DevRoleChangeRequest;
import com.myproject.video.video_platform.dto.user.UpdateUserRequest;
import com.myproject.video.video_platform.dto.user.UserDto;
import com.myproject.video.video_platform.service.security.AuthService;
import com.myproject.video.video_platform.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for handling user account actions.
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "Users", description = "Authenticated user profile management.")
public class UserController implements UserApiDoc {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Endpoint to retrieve the authenticated user's basic info.
     * Requires a valid JWT, so Spring Security sets Authentication.
     */
    @GetMapping(value = "/userInfo", produces= MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<UserDto> getUserInfo(Authentication authentication) {
        UserDto userDto = userService.getUserInfo(authentication);
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/userInfo")
    @Override
    public ResponseEntity<UserDto> updateUserInfo(@Valid @RequestBody UpdateUserRequest req) {
        UserDto updatedUser = userService.updateUserInfo(req);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/dev/role")
    public ResponseEntity<UserDto> changeDevRole(@Valid @RequestBody DevRoleChangeRequest req,
                                                 HttpServletResponse response) {
        UserDto updatedUser = userService.changeCurrentUserRoleForDev(req.getRole());
        authService.setAuthCookies(response, updatedUser.getEmail());
        return ResponseEntity.ok(updatedUser);
    }
}

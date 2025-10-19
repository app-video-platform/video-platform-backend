package com.myproject.video.video_platform.controller.user;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.user.UpdateUserRequest;
import com.myproject.video.video_platform.dto.user.UserDto;
import com.myproject.video.video_platform.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for handling user account actions.
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "Users", description = "Authenticated user profile management.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to retrieve the authenticated user's basic info.
     * Requires a valid JWT, so Spring Security sets Authentication.
     */
    @GetMapping(value = "/userInfo", produces= MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetch own profile", description = "Requires a valid JWT cookie. Returns the signed-in teacher's profile including social links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDto.class),
                            examples = @ExampleObject(value = "{\n  \"id\": \"738297f1-45fb-4f5f-98a5-6d0eb0a8f542\",\n  \"firstName\": \"Amelia\",\n  \"lastName\": \"Hughes\",\n  \"email\": \"amelia.hughes@example.com\",\n  \"title\": \"Lifestyle Photographer\"\n}"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserDto> getUserInfo(Authentication authentication) {
        UserDto userDto = userService.getUserInfo(authentication);
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/userInfo")
    @Operation(summary = "Update own profile", description = "Applies profile changes for the authenticated teacher. Ownership is enforced via the JWT subject.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Attempt to modify another user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserDto> updateUserInfo(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated profile fields",
            required = true,
            content = @Content(schema = @Schema(implementation = UpdateUserRequest.class),
                    examples = @ExampleObject(value = "{\n  \"userId\": \"738297f1-45fb-4f5f-98a5-6d0eb0a8f542\",\n  \"title\": \"Lifestyle Photographer\",\n  \"bio\": \"Helping creatives build their first paid lifestyle shoots.\"\n}"))) @Valid @RequestBody UpdateUserRequest req) {
        UserDto updatedUser = userService.updateUserInfo(req);
        return ResponseEntity.ok(updatedUser);
    }
}

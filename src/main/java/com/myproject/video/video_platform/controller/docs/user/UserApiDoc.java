package com.myproject.video.video_platform.controller.docs.user;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.user.UpdateUserRequest;
import com.myproject.video.video_platform.dto.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface UserApiDoc {

    @Operation(
            summary = "Fetch own profile",
            description = "Requires a valid JWT cookie. Returns the signed-in teacher's profile including social links."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDto.class),
                            examples = @ExampleObject(value = "{\n  \"id\": \"738297f1-45fb-4f5f-98a5-6d0eb0a8f542\",\n  \"firstName\": \"Amelia\",\n  \"lastName\": \"Hughes\",\n  \"email\": \"amelia.hughes@example.com\",\n  \"title\": \"Lifestyle Photographer\"\n}"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserDto> getUserInfo(Authentication authentication);

    @Operation(
            summary = "Update own profile",
            description = "Applies profile changes for the authenticated teacher. Ownership is enforced via the JWT subject.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated profile fields",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateUserRequest.class),
                            examples = @ExampleObject(
                                    value = "{\n  \"userId\": \"738297f1-45fb-4f5f-98a5-6d0eb0a8f542\",\n  \"title\": \"Lifestyle Photographer\",\n  \"bio\": \"Helping creatives build their first paid lifestyle shoots.\"\n}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Attempt to modify another user",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<UserDto> updateUserInfo(@Valid UpdateUserRequest req);
}

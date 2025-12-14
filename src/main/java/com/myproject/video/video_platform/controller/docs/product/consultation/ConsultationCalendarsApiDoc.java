package com.myproject.video.video_platform.controller.docs.product.consultation;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.products.consultation.CalendarDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface ConsultationCalendarsApiDoc {

    @Operation(
            summary = "List calendar providers",
            description = "Returns the providers the platform currently supports for consultation sync."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Providers returned",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CalendarDtos.ProviderListResponse.class))
    )
    ResponseEntity<CalendarDtos.ProviderListResponse> providers();

    @Operation(
            summary = "Init calendar connect",
            description = "Creates a signed OAuth state and redirect URL for the requested provider. Requires the teacher to be authenticated.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Provider selection and optional login hint",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CalendarDtos.ConnectInitRequest.class))
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connect flow initiated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CalendarDtos.ConnectInitResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    ResponseEntity<CalendarDtos.ConnectInitResponse> connectInit(CalendarDtos.ConnectInitRequest req);

    @Operation(
            summary = "Handle OAuth callback",
            description = "Completes the provider OAuth handshake, encrypts tokens, and persists the connected calendar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calendar connected",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CalendarDtos.ConnectedCalendarResponse.class))),
            @ApiResponse(responseCode = "400", description = "State mismatch or provider unsupported",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CalendarDtos.ConnectedCalendarResponse> oauthCallback(String provider, String code, String state);

    @Operation(
            summary = "List own calendars",
            description = "Returns every calendar the authenticated teacher has connected for consultation syncing."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Calendars returned",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CalendarDtos.ConnectedCalendarResponse.class))
    )
    ResponseEntity<?> myCalendars();

    @Operation(
            summary = "Disconnect calendar",
            description = "Removes a previously connected calendar. Requires ownership of the connection."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Calendar disconnected"),
            @ApiResponse(responseCode = "400", description = "Calendar not found or not owned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> delete(UUID id);
}


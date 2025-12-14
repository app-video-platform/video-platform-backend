package com.myproject.video.video_platform.controller.product.consultation;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.dto.products.consultation.CalendarDtos;
import com.myproject.video.video_platform.service.product.consultation.calendar.CalendarIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
@Tag(name = "Calendars", description = "Connect external calendars to power consultation availability.")
public class ConsultationCalendarsController {

    private final CalendarIntegrationService calendars;

    @GetMapping("/providers")
    @Operation(summary = "List calendar providers", description = "Returns the providers the platform currently supports for consultation sync.")
    @ApiResponse(responseCode = "200", description = "Providers returned",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CalendarDtos.ProviderListResponse.class)))
    public ResponseEntity<CalendarDtos.ProviderListResponse> providers() {
        return ResponseEntity.ok(calendars.listProviders());
    }

    @PostMapping("/connect")
    @Operation(summary = "Init calendar connect", description = "Creates a signed OAuth state and redirect URL for the requested provider. Requires the teacher to be authenticated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Connect flow initiated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CalendarDtos.ConnectInitResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    public ResponseEntity<CalendarDtos.ConnectInitResponse> connectInit(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Provider selection and optional login hint",
            required = true,
            content = @Content(schema = @Schema(implementation = CalendarDtos.ConnectInitRequest.class))) @Validated @RequestBody CalendarDtos.ConnectInitRequest req) {
        // CSRF: FE must include X-XSRF-TOKEN per your filter
        var resp = calendars.initConnect(req);
        return ResponseEntity.ok(resp);
    }

    // OAuth redirect callbacks (GET works for both Google & Microsoft)
    @GetMapping("/oauth/{provider}/callback")
    @Operation(summary = "Handle OAuth callback", description = "Completes the provider OAuth handshake, encrypts tokens, and persists the connected calendar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Calendar connected",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CalendarDtos.ConnectedCalendarResponse.class))),
            @ApiResponse(responseCode = "400", description = "State mismatch or provider unsupported",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CalendarDtos.ConnectedCalendarResponse> oauthCallback(
            @PathVariable String provider,
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {
        var resp = calendars.handleOAuthCallback(
                CalendarDtos.OAuthCallbackRequest.builder()
                        .code(code)
                        .state(state)
                        .provider(provider)
                        .build()
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    @Operation(summary = "List own calendars", description = "Returns every calendar the authenticated teacher has connected for consultation syncing.")
    @ApiResponse(responseCode = "200", description = "Calendars returned",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CalendarDtos.ConnectedCalendarResponse.class)))
    public ResponseEntity<?> myCalendars() {
        return ResponseEntity.ok(calendars.listMyCalendars());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Disconnect calendar", description = "Removes a previously connected calendar. Requires ownership of the connection.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Calendar disconnected"),
            @ApiResponse(responseCode = "400", description = "Calendar not found or not owned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        calendars.deleteMyCalendar(id);
        return ResponseEntity.noContent().build();
    }
}

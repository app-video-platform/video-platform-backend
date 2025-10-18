package com.myproject.video.video_platform.controller.product;

import com.myproject.video.video_platform.dto.products.consultation.CalendarDtos;
import com.myproject.video.video_platform.service.product.consultation.calendar.CalendarIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CalendarController {

    private final CalendarIntegrationService calendars;

    @GetMapping("/providers")
    public ResponseEntity<CalendarDtos.ProviderListResponse> providers() {
        return ResponseEntity.ok(calendars.listProviders());
    }

    @PostMapping("/connect")
    public ResponseEntity<CalendarDtos.ConnectInitResponse> connectInit(@Validated @RequestBody CalendarDtos.ConnectInitRequest req) {
        // CSRF: FE must include X-XSRF-TOKEN per your filter
        var resp = calendars.initConnect(req);
        return ResponseEntity.ok(resp);
    }

    // OAuth redirect callbacks (GET works for both Google & Microsoft)
    @GetMapping("/oauth/{provider}/callback")
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
    public ResponseEntity<?> myCalendars() {
        return ResponseEntity.ok(calendars.listMyCalendars());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        calendars.deleteMyCalendar(id);
        return ResponseEntity.noContent().build();
    }
}

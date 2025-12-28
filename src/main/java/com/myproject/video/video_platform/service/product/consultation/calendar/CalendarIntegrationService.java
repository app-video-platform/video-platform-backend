package com.myproject.video.video_platform.service.product.consultation.calendar;

import com.myproject.video.video_platform.service.security.OAuthState;
import com.myproject.video.video_platform.service.security.TokenCrypto;
import com.myproject.video.video_platform.dto.products.consultation.CalendarDtos;
import com.myproject.video.video_platform.entity.products.consultation.ConnectedCalendar;
import com.myproject.video.video_platform.repository.products.consultation.ConnectedCalendarRepository;
import com.myproject.video.video_platform.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarIntegrationService {

    private final CurrentUserService currentUserService;
    private final ConnectedCalendarRepository repo;
    private final OAuthState stateUtil;
    private final TokenCrypto crypto;
    private final GoogleCalendarClient google;
    private final MicrosoftCalendarClient microsoft;
    private final ICloudCalendarClient icloud;

    public CalendarDtos.ProviderListResponse listProviders() {
        return CalendarDtos.ProviderListResponse.builder()
                .providers(new String[]{"GOOGLE", "MICROSOFT", "ICLOUD"})
                .build();
    }

    public CalendarDtos.ConnectInitResponse initConnect(CalendarDtos.ConnectInitRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        ConnectedCalendar.Provider provider = ConnectedCalendar.Provider.valueOf(request.getProvider().toUpperCase());

        // Sign ephemeral state bound to user and provider (5 min expiry)
        var payload = new OAuthState.Payload(
                userId.toString(),
                provider.name(),
                Instant.now().plusSeconds(300).getEpochSecond(),
                UUID.randomUUID().toString()
        );
        String state = stateUtil.sign(payload);

        String url;
        switch (provider) {
            case GOOGLE -> url = google.buildAuthorizationUrl(state, request.getLoginHint());
            case MICROSOFT -> url = microsoft.buildAuthorizationUrl(state, request.getLoginHint());
            case ICLOUD -> url = null;

            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        log.info("Calendar connect init: provider={}, userId={}", provider, userId);
        return CalendarDtos.ConnectInitResponse.builder().authorizationUrl(url).state(state).build();
    }

    @Transactional
    public CalendarDtos.ConnectedCalendarResponse handleOAuthCallback(CalendarDtos.OAuthCallbackRequest request) {
        // Verify state binds to this user and provider
        var st = stateUtil.verify(request.getState());
        ConnectedCalendar.Provider provider = ConnectedCalendar.Provider.valueOf(request.getProvider().toUpperCase());
        if (!st.getProvider().equals(provider.name())) throw new IllegalArgumentException("Provider mismatch in state");

        UUID userId = currentUserService.getCurrentUserId();
        if (!st.getUserId().equals(userId.toString())) throw new IllegalArgumentException("State not for this user");

        CalendarProviderClient.TokenBundle tokens;
        switch (provider) {
            case GOOGLE -> tokens = google.exchangeCodeForTokens(request.getCode());
            case MICROSOFT -> tokens = microsoft.exchangeCodeForTokens(request.getCode());
            default -> throw new IllegalArgumentException("Unsupported provider for OAuth: " + provider);
        }

        // Encrypt before persisting
        String encAccess = crypto.encrypt(tokens.accessToken());
        String encRefresh = tokens.refreshToken() != null ? crypto.encrypt(tokens.refreshToken()) : null;

        ConnectedCalendar cc = new ConnectedCalendar();
        cc.setTeacherId(userId);
        cc.setProvider(provider);
        cc.setOauthTokenEnc(encAccess);
        cc.setRefreshTokenEnc(encRefresh);
        if (tokens.expiresAt() != null) {
            cc.setExpiresAt(ZonedDateTime.ofInstant(tokens.expiresAt(), java.time.ZoneOffset.UTC));
        }
        // createdAt/updatedAt by DB default or @PrePersist if you have it

        repo.save(cc);

        log.info("Calendar connected: provider={}, calendarId={}, userId={}", provider, cc.getId(), userId);

        return CalendarDtos.ConnectedCalendarResponse.builder()
                .id(cc.getId())
                .provider(provider.name())
                .accountEmail(null) // TODO: google.getAccountEmail(tokens.accessToken())
                .expiresAt(tokens.expiresAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CalendarDtos.ConnectedCalendarResponse> listMyCalendars() {
        UUID userId = currentUserService.getCurrentUserId();
        return repo.findAllByTeacherId(userId).stream()
                .map(c -> CalendarDtos.ConnectedCalendarResponse.builder()
                        .id(c.getId())
                        .provider(c.getProvider().name())
                        .accountEmail(null) // optional
                        .expiresAt(c.getExpiresAt().toInstant())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteMyCalendar(UUID id) {
        UUID userId = currentUserService.getCurrentUserId();
        var cal = repo.findById(id)
                .filter(c -> c.getTeacherId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Calendar not found or not owned"));

        // TODO: optionally revoke tokens at provider
        repo.delete(cal);
        log.info("Calendar disconnected: provider={}, calendarId={}, userId={}", cal.getProvider(), cal.getId(), userId);
    }
}

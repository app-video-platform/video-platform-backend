package com.myproject.video.video_platform.service.product.consultation.calendar;

import com.myproject.video.video_platform.service.security.calendar.CalendarProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MicrosoftCalendarClient implements CalendarProviderClient {

    private final CalendarProperties props;
    private final WebClient web = WebClient.builder().build();

    @Override
    public String buildAuthorizationUrl(String state, String loginHint) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromUriString("https://login.microsoftonline.com/" + props.getMicrosoft().getTenant() + "/oauth2/v2.0/authorize")
                .queryParam("client_id", props.getMicrosoft().getClientId())
                .queryParam("redirect_uri", props.getMicrosoft().getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("response_mode", "query")
                .queryParam("scope", String.join(" ",
                        "offline_access", "Calendars.Read", "Calendars.ReadWrite"))
                .queryParam("state", state);
        if (loginHint != null && !loginHint.isBlank()) b.queryParam("login_hint", loginHint);
        return b.build(true).toUriString();
    }

    @Override
    public TokenBundle exchangeCodeForTokens(String code) {
        Map<String, Object> tokenResp = web.post()
                .uri(URI.create("https://login.microsoftonline.com/" + props.getMicrosoft().getTenant() + "/oauth2/v2.0/token"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("client_id=" + props.getMicrosoft().getClientId() +
                        "&scope=" + "offline_access Calendars.Read Calendars.ReadWrite" +
                        "&code=" + code +
                        "&redirect_uri=" + props.getMicrosoft().getRedirectUri() +
                        "&grant_type=authorization_code" +
                        "&client_secret=" + props.getMicrosoft().getClientSecret())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String accessToken = (String) tokenResp.get("access_token");
        String refreshToken = (String) tokenResp.get("refresh_token");
        Integer expiresIn = (Integer) tokenResp.get("expires_in");
        Instant expiresAt = expiresIn != null ? Instant.now().plusSeconds(expiresIn) : null;

        return new TokenBundle(accessToken, refreshToken, expiresAt);
    }

    @Override
    public String getAccountEmail(String accessToken) {
        // TODO: call Microsoft Graph /me to fetch mail/UPN.
        return null;
    }
}

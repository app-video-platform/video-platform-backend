package com.myproject.video.video_platform.service.product.consultation.calendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.myproject.video.video_platform.exception.auth.OAuthErrorResponse;
import com.myproject.video.video_platform.exception.auth.OAuthTokenExchangeException;
import com.myproject.video.video_platform.service.security.calendar.CalendarProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarClient implements CalendarProviderClient {

    private static final Duration TOKEN_TIMEOUT = Duration.ofSeconds(10);
    private final CalendarProperties props;
    private final WebClient web = WebClient.builder().build();

    @Override
    public String buildAuthorizationUrl(String state, String loginHint) {
        UriComponentsBuilder b = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", props.getGoogle().getClientId())
                .queryParam("redirect_uri", props.getGoogle().getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("scope", String.join(" ",
                        "https://www.googleapis.com/auth/calendar.readonly",
                        "https://www.googleapis.com/auth/calendar.events"))
                .queryParam("state", state);
        if (loginHint != null && !loginHint.isBlank()) b.queryParam("login_hint", loginHint);
        return b.build().toUriString();
    }

    @Override
    public TokenBundle exchangeCodeForTokens(String code) {
        // Build form body (safer than manual string concatenation)
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", props.getGoogle().getClientId());
        form.add("client_secret", props.getGoogle().getClientSecret());
        form.add("redirect_uri", props.getGoogle().getRedirectUri());
        form.add("grant_type", "authorization_code");

        try {
            GoogleTokenResponse token = web.post()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .retrieve()
                    // Handle non-2xx with a sanitized message
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            resp -> resp.bodyToMono(OAuthErrorResponse.class)
                                    .defaultIfEmpty(new OAuthErrorResponse()) // might be empty body
                                    .map(err -> {
                                        String codeName = (err.getError() != null ? err.getError() : "unknown_error");
                                        String desc = (err.getError_description() != null ? err.getError_description() : "");
                                        // Log without secrets (we're not logging the 'code' parameter or client_secret)
                                        log.warn("Google token exchange failed: status={}, error={}, desc={}",
                                                resp.statusCode().value(), codeName, truncate(desc, 300));
                                        return new OAuthTokenExchangeException(
                                                "Google token exchange failed: " + codeName);
                                    }))
                    .bodyToMono(GoogleTokenResponse.class)
                    .timeout(TOKEN_TIMEOUT)
                    .block();

            if (token == null || token.getAccessToken() == null || token.getAccessToken().isBlank()) {
                log.warn("Google token exchange returned no access_token");
                throw new OAuthTokenExchangeException("Google token exchange returned no access_token");
            }

            Long expiresIn = token.getExpiresInSeconds();
            Instant expiresAt = (expiresIn != null ? Instant.now().plusSeconds(expiresIn) : null);

            return new TokenBundle(token.getAccessToken(), token.getRefreshToken(), expiresAt);

        } catch (WebClientResponseException e) {
            // Non-2xx with body not parsed by onStatus (e.g., non-JSON)
            log.warn("Google token HTTP error: status={}, bodyLen={}",
                    e.getRawStatusCode(), (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString().length() : 0));
            throw new OAuthTokenExchangeException("Google token HTTP error: " + e.getRawStatusCode());
        } catch (Exception e) {
            // Generic failure
            log.error("Google token exchange unexpected failure (sanitized)", e);
            throw new OAuthTokenExchangeException("Google token exchange failed");
        }
    }

    private static String truncate(String s, int max) {
        return (s == null ? "" : (s.length() <= max ? s : s.substring(0, max) + "…"));
    }

    @Override
    public String getAccountEmail(String accessToken) {
        // Minimal call: users.me or calendar settings could expose primary email
        // TODO: call Google People API or Calendar settings API to infer email.
        return null;
    }
}

package com.myproject.video.video_platform.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class OAuthState {

    private final byte[] secret;
    private final ObjectMapper mapper = new ObjectMapper();

    public OAuthState(@org.springframework.beans.factory.annotation.Value("${app.calendar.stateSecret}") String secret) {
        this.secret = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private String userId;
        private String provider;
        private long exp; // epoch seconds
        private String nonce;
    }

    public String sign(Payload payload) {
        try {
            String body = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mapper.writeValueAsBytes(Map.of(
                            "uid", payload.getUserId(),
                            "prv", payload.getProvider(),
                            "exp", payload.getExp(),
                            "nnc", payload.getNonce()
                    )));
            String sig = hmac(body);
            return body + "." + sig;
        } catch (Exception e) {
            throw new IllegalStateException("State sign error", e);
        }
    }

    public Payload verify(String token) {
        try {
            int dot = token.lastIndexOf('.');
            if (dot < 0) throw new IllegalArgumentException("Invalid state");
            String body = token.substring(0, dot);
            String sig = token.substring(dot + 1);
            if (!hmac(body).equals(sig)) throw new IllegalArgumentException("Invalid state signature");

            byte[] bytes = Base64.getUrlDecoder().decode(body);
            Map<?,?> map = mapper.readValue(bytes, Map.class);

            String uid = (String) map.get("uid");
            String prv = (String) map.get("prv");
            long exp = ((Number) map.get("exp")).longValue();
            String nnc = (String) map.get("nnc");
            if (Instant.now().getEpochSecond() > exp) throw new IllegalArgumentException("State expired");

            return new Payload(uid, prv, exp, nnc);
        } catch (Exception e) {
            throw new IllegalArgumentException("State verify error", e);
        }
    }

    private String hmac(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(body.getBytes()));
    }
}

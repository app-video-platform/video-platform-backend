package com.myproject.video.video_platform.service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Component
public class TokenCrypto {
    private static final String ALG = "AES";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final byte[] key;

    public TokenCrypto(@org.springframework.beans.factory.annotation.Value("${app.calendar.cryptoSecret}") String keyB64) {
        this.key = Base64.getDecoder().decode(keyB64);
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALG), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            log.error("Token encryption failed", e);
            throw new IllegalStateException("Encryption error");
        }
    }

    public String decrypt(String ciphertextB64) {
        try {
            byte[] in = Base64.getDecoder().decode(ciphertextB64);
            byte[] iv = java.util.Arrays.copyOfRange(in, 0, IV_BYTES);
            byte[] ct = java.util.Arrays.copyOfRange(in, IV_BYTES, in.length);

            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALG), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] pt = cipher.doFinal(ct);
            return new String(pt, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Token decryption failed", e);
            throw new IllegalStateException("Decryption error");
        }
    }
}

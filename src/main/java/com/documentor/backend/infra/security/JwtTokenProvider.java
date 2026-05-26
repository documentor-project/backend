package com.documentor.backend.infra.security;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final byte[] secret;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds,
            @Value("${app.jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenExpirationSeconds);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenExpirationSeconds);
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }

    public Long extractUserId(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }

        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        long expiresAt = extractLong(payload, "exp");
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "만료된 토큰입니다.");
        }

        return extractLong(payload, "sub");
    }

    private String createToken(Long userId, long expirationSeconds) {
        long expiresAt = Instant.now().plusSeconds(expirationSeconds).getEpochSecond();
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":" + userId + ",\"exp\":" + expiresAt + "}");
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + sign(unsignedToken);
    }

    private String encode(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign JWT.", exception);
        }
    }

    private long extractLong(String payload, String fieldName) {
        String key = "\"" + fieldName + "\":";
        int start = payload.indexOf(key);
        if (start < 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
        int valueStart = start + key.length();
        int valueEnd = payload.indexOf(',', valueStart);
        if (valueEnd < 0) {
            valueEnd = payload.indexOf('}', valueStart);
        }
        return Long.parseLong(payload.substring(valueStart, valueEnd));
    }

    private boolean constantTimeEquals(String left, String right) {
        return MessageDigestSupport.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}

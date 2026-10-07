package in.aarogya.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import in.aarogya.identity.domain.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    public static final String ACCESS_TYPE = "access";
    public static final String REFRESH_TYPE = "refresh";

    private final SecretKey key;
    private final Duration accessDuration;
    private final Duration refreshDuration;

    public JwtService(
        @Value("${app.security.jwt-secret}") String secret,
        @Value("${app.security.access-token-minutes:15}") long accessTokenMinutes,
        @Value("${app.security.refresh-token-days:30}") long refreshTokenDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessDuration = Duration.ofMinutes(accessTokenMinutes);
        this.refreshDuration = Duration.ofDays(refreshTokenDays);
    }

    public TokenValue issueAccessToken(UserAccount account) {
        var now = Instant.now();
        var expiresAt = now.plus(accessDuration);

        var token = Jwts.builder()
            .subject(account.getId().toString())
            .claim("email", account.getEmail())
            .claim("name", account.getDisplayName())
            .claim("role", account.getRole().name())
            .claim("type", ACCESS_TYPE)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key, Jwts.SIG.HS256)
            .compact();

        return new TokenValue(token, expiresAt);
    }

    public TokenValue issueRefreshToken(UserAccount account) {
        var now = Instant.now();
        var expiresAt = now.plus(refreshDuration);

        var token = Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(account.getId().toString())
            .claim("type", REFRESH_TYPE)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(key, Jwts.SIG.HS256)
            .compact();

        return new TokenValue(token, expiresAt);
    }

    public Claims parseAccessToken(String token) {
        return parse(token, ACCESS_TYPE);
    }

    public Claims parseRefreshToken(String token) {
        return parse(token, REFRESH_TYPE);
    }

    public UUID subjectAsUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    private Claims parse(String token, String expectedType) {
        var claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        if (!expectedType.equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Unexpected token type");
        }

        return claims;
    }

    public record TokenValue(String value, Instant expiresAt) {
    }
}

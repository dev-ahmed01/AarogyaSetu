package in.aarogya.identity.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthCookieService {

    public static final String ACCESS_COOKIE = "aarogya_access";
    public static final String REFRESH_COOKIE = "aarogya_refresh";

    private final boolean secure;
    private final String sameSite;

    public AuthCookieService(
        @Value("${app.security.cookie-secure:false}") boolean secure,
        @Value("${app.security.cookie-same-site:Lax}") String sameSite
    ) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public void writeSessionCookies(
        HttpServletResponse response,
        AuthService.SessionBundle session
    ) {
        addCookie(
            response,
            ACCESS_COOKIE,
            session.accessToken(),
            "/",
            durationUntil(session.accessExpiresAt())
        );

        addCookie(
            response,
            REFRESH_COOKIE,
            session.refreshToken(),
            "/api/auth",
            durationUntil(session.refreshExpiresAt())
        );
    }

    public void clearSessionCookies(HttpServletResponse response) {
        addCookie(response, ACCESS_COOKIE, "", "/", Duration.ZERO);
        addCookie(response, REFRESH_COOKIE, "", "/api/auth", Duration.ZERO);
    }

    private void addCookie(
        HttpServletResponse response,
        String name,
        String value,
        String path,
        Duration maxAge
    ) {
        var cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path(path)
            .maxAge(maxAge)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private Duration durationUntil(Instant expiresAt) {
        var duration = Duration.between(Instant.now(), expiresAt);
        return duration.isNegative() ? Duration.ZERO : duration;
    }
}

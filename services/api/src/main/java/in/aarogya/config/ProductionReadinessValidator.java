package in.aarogya.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductionReadinessValidator implements ApplicationRunner {

    private static final String LOCAL_JWT_SECRET =
        "aarogya-local-development-secret-change-before-production-2026";

    private final String environment;
    private final String jwtSecret;
    private final boolean cookieSecure;
    private final String sameSite;
    private final String allowedOrigins;
    private final String databaseUrl;
    private final String databasePassword;

    public ProductionReadinessValidator(
        @Value("${app.runtime.environment:local}") String environment,
        @Value("${app.security.jwt-secret}") String jwtSecret,
        @Value("${app.security.cookie-secure:false}") boolean cookieSecure,
        @Value("${app.security.cookie-same-site:Lax}") String sameSite,
        @Value("${app.cors.allowed-origins:http://localhost:3000}") String allowedOrigins,
        @Value("${spring.datasource.url}") String databaseUrl,
        @Value("${spring.datasource.password}") String databasePassword
    ) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.cookieSecure = cookieSecure;
        this.sameSite = sameSite;
        this.allowedOrigins = allowedOrigins;
        this.databaseUrl = databaseUrl;
        this.databasePassword = databasePassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!"production".equalsIgnoreCase(environment.trim())) {
            return;
        }

        var errors = new ArrayList<String>();

        if (jwtSecret == null
            || jwtSecret.length() < 64
            || LOCAL_JWT_SECRET.equals(jwtSecret)) {
            errors.add("JWT_SECRET must be a unique value of at least 64 characters.");
        }

        if (!cookieSecure) {
            errors.add("COOKIE_SECURE must be true.");
        }

        if ("none".equalsIgnoreCase(sameSite.trim())) {
            errors.add(
                "COOKIE_SAME_SITE=None is not supported while cookie-authenticated CSRF protection is not enabled. Use Lax/Strict with same-site web and API domains."
            );
        }

        var origins = Arrays.stream(allowedOrigins.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .toList();

        if (origins.isEmpty()
            || origins.stream().anyMatch(origin ->
                "*".equals(origin)
                    || origin.contains("localhost")
                    || origin.contains("127.0.0.1")
                    || !origin.toLowerCase(Locale.ROOT).startsWith("https://")
            )) {
            errors.add(
                "CORS_ALLOWED_ORIGINS must contain explicit HTTPS production origins only."
            );
        }

        if (databaseUrl == null
            || databaseUrl.contains("localhost")
            || databaseUrl.contains("127.0.0.1")) {
            errors.add("DATABASE_URL must point to a non-local production database.");
        }

        if (databasePassword == null
            || databasePassword.isBlank()
            || "aarogya_local".equals(databasePassword)) {
            errors.add("POSTGRES_PASSWORD must not use the local development value.");
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                "Production configuration rejected: " + String.join(" ", errors)
            );
        }
    }
}

package in.aarogya.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh"
    );

    private final ConcurrentHashMap<String, Window> windows =
        new ConcurrentHashMap<>();

    private final int maxAttempts;
    private final long windowSeconds;

    public AuthRateLimitFilter(
        @Value("${app.security.auth-rate-limit-attempts:20}") int maxAttempts,
        @Value("${app.security.auth-rate-limit-window-seconds:300}") long windowSeconds
    ) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.windowSeconds = Math.max(1, windowSeconds);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
            || !LIMITED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        var now = Instant.now().getEpochSecond();
        var key = request.getRemoteAddr() + ":" + request.getRequestURI();

        var state = windows.compute(key, (ignored, current) -> {
            if (current == null || now >= current.resetAtEpochSecond()) {
                return new Window(1, now + windowSeconds);
            }

            return new Window(
                current.count() + 1,
                current.resetAtEpochSecond()
            );
        });

        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(
                entry -> now >= entry.getValue().resetAtEpochSecond()
            );
        }

        response.setHeader(
            "X-RateLimit-Limit",
            Integer.toString(maxAttempts)
        );
        response.setHeader(
            "X-RateLimit-Remaining",
            Integer.toString(Math.max(0, maxAttempts - state.count()))
        );

        if (state.count() > maxAttempts) {
            var retryAfter = Math.max(
                1,
                state.resetAtEpochSecond() - now
            );

            response.setStatus(429);
            response.setHeader(
                "Retry-After",
                Long.toString(retryAfter)
            );
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                "{\"status\":429,\"error\":\"RATE_LIMITED\","
                    + "\"message\":\"Too many authentication attempts. Try again later.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private record Window(
        int count,
        long resetAtEpochSecond
    ) {
    }
}

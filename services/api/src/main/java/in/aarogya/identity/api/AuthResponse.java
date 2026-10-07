package in.aarogya.identity.api;

import java.time.Instant;

public record AuthResponse(
    UserResponse user,
    Instant accessExpiresAt
) {
}

package in.aarogya.health.api;

import java.time.Instant;

public record HealthConsentResponse(
    String purposeCode,
    boolean granted,
    String policyVersion,
    Instant recordedAt
) {
}

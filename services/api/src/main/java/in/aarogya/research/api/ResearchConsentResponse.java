package in.aarogya.research.api;

import java.time.Instant;

public record ResearchConsentResponse(
    boolean granted,
    String policyVersion,
    Instant recordedAt,
    boolean instrumentationEnabled
) {
}

package in.aarogya.health.api;

import java.time.Instant;

public record HealthIntegrationResponse(
    String providerCode,
    String displayName,
    String integrationMode,
    String status,
    boolean liveConnectivity,
    String interoperabilityStandard,
    String externalSubjectRef,
    Instant connectedAt,
    Instant disconnectedAt,
    Instant lastImportedAt,
    String disclaimer
) {
}

package in.aarogya.health.api;

import java.time.Instant;

public record HealthImportResponse(
    int imported,
    int skippedExisting,
    Instant importedAt,
    String sourceSystem,
    boolean liveConnectivity,
    String message
) {
}

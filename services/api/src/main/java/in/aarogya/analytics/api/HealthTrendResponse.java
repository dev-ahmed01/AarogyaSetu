package in.aarogya.analytics.api;

import java.util.List;

public record HealthTrendResponse(
    boolean analysisConsentGranted,
    String observationCode,
    String label,
    String sourceType,
    String status,
    String notice,
    List<HealthTrendPointResponse> points
) {
}

package in.aarogya.health.api;

import java.time.LocalDate;

public record HealthSummaryResponse(
    long recordCount,
    long observationCount,
    LocalDate latestClinicalDate,
    HealthConsentResponse analysisConsent
) {
}

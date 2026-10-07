package in.aarogya.health.integration;

import java.math.BigDecimal;
import java.time.Instant;

public record ExternalHealthObservation(
    String sourceObservationRef,
    String observationCode,
    String codingSystem,
    String displayName,
    BigDecimal valueNumeric,
    String valueText,
    String unit,
    String referenceRangeText,
    Instant observedAt
) {
}

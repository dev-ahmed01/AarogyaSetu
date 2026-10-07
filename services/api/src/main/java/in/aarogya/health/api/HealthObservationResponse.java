package in.aarogya.health.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import in.aarogya.health.domain.HealthObservation;

public record HealthObservationResponse(
    UUID id,
    String code,
    String codingSystem,
    String displayName,
    BigDecimal valueNumeric,
    String valueText,
    String unit,
    String referenceRangeText,
    Instant observedAt,
    String sourceObservationRef
) {

    public static HealthObservationResponse from(HealthObservation observation) {
        return new HealthObservationResponse(
            observation.getId(),
            observation.getObservationCode(),
            observation.getCodingSystem(),
            observation.getDisplayName(),
            observation.getValueNumeric(),
            observation.getValueText(),
            observation.getUnit(),
            observation.getReferenceRangeText(),
            observation.getObservedAt(),
            observation.getSourceObservationRef()
        );
    }
}

package in.aarogya.health.api;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HealthObservationRequest(
    @NotBlank @Size(max = 100) String code,
    @NotBlank @Size(max = 180) String displayName,
    BigDecimal valueNumeric,
    @Size(max = 500) String valueText,
    @Size(max = 40) String unit,
    @Size(max = 160) String referenceRangeText,
    Instant observedAt
) {
}

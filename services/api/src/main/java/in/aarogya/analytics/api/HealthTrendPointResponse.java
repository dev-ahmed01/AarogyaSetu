package in.aarogya.analytics.api;

import java.math.BigDecimal;
import java.time.Instant;

public record HealthTrendPointResponse(
    Instant observedAt,
    BigDecimal value,
    String unit
) {
}

package in.aarogya.research.api;

import java.math.BigDecimal;

public record ResearchMetricResponse(
    String metricCode,
    String label,
    BigDecimal value,
    String unit,
    int eligibleParticipants,
    boolean suppressed,
    String interpretation
) {
}

package in.aarogya.recommendations.api;

import java.math.BigDecimal;

public record RecommendationItemResponse(
    String ruleCode,
    int ruleVersion,
    int priority,
    String title,
    String reasonCode,
    String safetyClass,
    String observation,
    String whyItMatters,
    String consideration,
    BigDecimal observedValue,
    BigDecimal referenceValue,
    String unit,
    EvidenceSourceResponse evidence
) {
}

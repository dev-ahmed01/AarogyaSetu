package in.aarogya.research.api;

import in.aarogya.research.domain.ResearchMetricDefinition;

public record ResearchMetricDefinitionResponse(
    String metricCode,
    int metricVersion,
    String name,
    String description,
    String unit,
    Integer windowDays,
    int minimumCohortSize
) {
    public static ResearchMetricDefinitionResponse from(
        ResearchMetricDefinition definition
    ) {
        return new ResearchMetricDefinitionResponse(
            definition.getMetricCode(),
            definition.getMetricVersion(),
            definition.getName(),
            definition.getDescription(),
            definition.getUnit(),
            definition.getWindowDays(),
            definition.getMinimumCohortSize()
        );
    }
}

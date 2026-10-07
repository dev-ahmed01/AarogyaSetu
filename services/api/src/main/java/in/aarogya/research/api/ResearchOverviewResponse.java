package in.aarogya.research.api;

import java.time.LocalDate;
import java.util.List;

public record ResearchOverviewResponse(
    LocalDate from,
    LocalDate to,
    int minimumCohortSize,
    int optedInParticipants,
    List<ResearchMetricResponse> metrics,
    List<ResearchExposureResponse> featureExposures,
    List<ResearchCohortResponse> dietaryPatternCohorts,
    List<String> notices
) {
}

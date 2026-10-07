package in.aarogya.recommendations.api;

import java.time.LocalDate;
import java.util.List;

public record RecommendationAssessmentResponse(
    String status,
    LocalDate assessmentDate,
    LocalDate analysisFrom,
    LocalDate analysisTo,
    int windowDays,
    int observedDays,
    int minimumTrendDays,
    List<RecommendationItemResponse> recommendations,
    List<String> notices
) {
}

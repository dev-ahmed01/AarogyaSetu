package in.aarogya.analytics.api;

import java.time.LocalDate;
import java.util.List;

public record LongitudinalAnalyticsResponse(
    LocalDate asOf,
    int windowDays,
    LocalDate currentFrom,
    LocalDate currentTo,
    LocalDate previousFrom,
    LocalDate previousTo,
    AnalyticsCoverageResponse coverage,
    AnalyticsCoverageResponse previousCoverage,
    int currentEntryCount,
    int previousEntryCount,
    List<DailyNutritionPointResponse> dailyNutrition,
    List<NutrientTrendResponse> nutrientTrends,
    List<MealPatternResponse> mealPatterns,
    HealthTrendResponse healthTrend,
    List<String> insights,
    List<String> dataQualityNotices
) {
}

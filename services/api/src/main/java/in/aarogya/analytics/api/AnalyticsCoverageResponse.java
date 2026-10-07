package in.aarogya.analytics.api;

public record AnalyticsCoverageResponse(
    int windowDays,
    int loggedDays,
    int coveragePercent,
    String status,
    String label
) {
}

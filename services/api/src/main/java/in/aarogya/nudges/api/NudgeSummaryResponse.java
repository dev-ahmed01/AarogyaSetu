package in.aarogya.nudges.api;

public record NudgeSummaryResponse(
    long activeCount,
    long attentionCount,
    long snoozedCount
) {
}

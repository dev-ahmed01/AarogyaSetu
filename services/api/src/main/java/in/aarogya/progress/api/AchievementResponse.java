package in.aarogya.progress.api;

import java.time.Instant;

public record AchievementResponse(
    String code,
    String title,
    String description,
    String criteriaCode,
    int thresholdValue,
    boolean earned,
    Instant earnedAt,
    Integer evidenceValue
) {
}

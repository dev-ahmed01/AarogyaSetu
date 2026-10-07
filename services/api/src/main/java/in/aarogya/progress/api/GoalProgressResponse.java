package in.aarogya.progress.api;

import java.time.LocalDate;
import java.util.UUID;

public record GoalProgressResponse(
    UUID id,
    String goalCode,
    String title,
    String description,
    String status,
    int targetValue,
    int currentValue,
    int progressPercent,
    String periodCode,
    LocalDate periodStart,
    LocalDate periodEnd,
    String progressLabel
) {
}

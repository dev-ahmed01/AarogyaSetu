package in.aarogya.progress.api;

import java.time.LocalDate;
import java.util.List;

public record ProgressOverviewResponse(
    LocalDate weekStart,
    LocalDate weekEnd,
    GoalProgressResponse mealLoggingGoal,
    StreakResponse streak,
    List<AchievementResponse> achievements,
    String philosophy
) {
}

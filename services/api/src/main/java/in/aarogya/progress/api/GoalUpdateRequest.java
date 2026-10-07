package in.aarogya.progress.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GoalUpdateRequest(
    @Min(2) @Max(7) int targetDaysPerWeek
) {
}

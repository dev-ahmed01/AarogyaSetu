package in.aarogya.nudges.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record NudgeSnoozeRequest(
    @Min(1) @Max(168) int hours
) {
}

package in.aarogya.plans.api;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record GeneratePlanRequest(
    @NotNull LocalDate planDate
) {
}

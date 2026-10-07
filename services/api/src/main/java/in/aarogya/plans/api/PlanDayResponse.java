package in.aarogya.plans.api;

import java.time.LocalDate;

public record PlanDayResponse(
    LocalDate date,
    boolean exists,
    DietPlanResponse plan
) {
}

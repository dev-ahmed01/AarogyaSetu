package in.aarogya.meals.api;

import java.time.LocalDate;
import java.util.List;

public record MealHistoryResponse(
    LocalDate from,
    LocalDate to,
    List<DailyMealLogResponse> days
) {
}

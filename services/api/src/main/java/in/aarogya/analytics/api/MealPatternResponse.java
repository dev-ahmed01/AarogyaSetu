package in.aarogya.analytics.api;

public record MealPatternResponse(
    String mealType,
    int entryCount,
    int daysPresent
) {
}

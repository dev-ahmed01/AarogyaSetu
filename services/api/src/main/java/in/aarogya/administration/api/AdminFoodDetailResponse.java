package in.aarogya.administration.api;

import java.util.List;
import java.util.Set;

public record AdminFoodDetailResponse(
    AdminFoodSummaryResponse summary,
    String description,
    String foodType,
    String category,
    String dietaryClassification,
    Set<String> allergens,
    Set<String> aliases,
    AdminSourceResponse source,
    String sourceFoodRef,
    List<AdminNutrientResponse> nutrients,
    List<AdminPortionResponse> portions,
    List<AdminReviewResponse> reviews
) {
}

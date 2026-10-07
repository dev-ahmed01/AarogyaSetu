package in.aarogya.plans.api;

import java.math.BigDecimal;

public record SmartFoodSuggestionResponse(
    String foodSlug,
    String foodName,
    String category,
    String dietaryClassification,
    String primaryRegion,
    String portionLabel,
    BigDecimal quantityGrams,
    String nutrientFocusCode,
    BigDecimal focusNutrientAmount,
    String focusNutrientUnit,
    String reasonCode,
    String explanation,
    String sourceCode,
    String sourceFoodRef,
    Integer regionalFitScore,
    String regionalFitLabel,
    String regionalReason
) {
}

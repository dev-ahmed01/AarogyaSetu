package in.aarogya.meals.api;

import java.math.BigDecimal;

import in.aarogya.meals.domain.MealEntryNutrient;

public record MealNutrientResponse(
    String code,
    BigDecimal amount,
    String unit
) {

    public static MealNutrientResponse from(MealEntryNutrient nutrient) {
        return new MealNutrientResponse(
            nutrient.getNutrientCode(),
            nutrient.getAmount(),
            nutrient.getUnit()
        );
    }
}

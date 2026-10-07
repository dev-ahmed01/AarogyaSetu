package in.aarogya.administration.api;

import java.math.BigDecimal;

import in.aarogya.nutrition.domain.FoodNutrient;

public record AdminNutrientResponse(
    String code,
    BigDecimal amountPer100g,
    String unit,
    String sourceCode,
    String sourceFoodRef
) {
    public static AdminNutrientResponse from(FoodNutrient nutrient) {
        return new AdminNutrientResponse(
            nutrient.getNutrientCode(),
            nutrient.getAmountPer100g(),
            nutrient.getUnit(),
            nutrient.getSource() == null
                ? null
                : nutrient.getSource().getSourceCode(),
            nutrient.getSourceFoodRef()
        );
    }
}

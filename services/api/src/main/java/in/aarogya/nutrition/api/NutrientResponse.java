package in.aarogya.nutrition.api;

import java.math.BigDecimal;

import in.aarogya.nutrition.domain.FoodNutrient;

public record NutrientResponse(
    String code,
    BigDecimal amountPer100g,
    String unit,
    String sourceCode,
    String sourceFoodRef
) {

    public static NutrientResponse from(FoodNutrient nutrient) {
        return new NutrientResponse(
            nutrient.getNutrientCode(),
            nutrient.getAmountPer100g(),
            nutrient.getUnit(),
            nutrient.getSource() == null ? null : nutrient.getSource().getSourceCode(),
            nutrient.getSourceFoodRef()
        );
    }
}

package in.aarogya.plans.api;

import java.math.BigDecimal;

import in.aarogya.plans.domain.DietPlanItemNutrient;

public record PlanNutrientResponse(
    String code,
    BigDecimal amount,
    String unit
) {

    public static PlanNutrientResponse from(DietPlanItemNutrient nutrient) {
        return new PlanNutrientResponse(
            nutrient.getNutrientCode(),
            nutrient.getAmount(),
            nutrient.getUnit()
        );
    }
}

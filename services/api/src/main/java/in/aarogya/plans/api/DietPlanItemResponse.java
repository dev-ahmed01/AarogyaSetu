package in.aarogya.plans.api;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import in.aarogya.plans.domain.DietPlanItem;
import in.aarogya.plans.domain.DietPlanItemNutrient;

public record DietPlanItemResponse(
    UUID id,
    String mealType,
    int displayOrder,
    String foodSlug,
    String foodName,
    String dietaryClassification,
    UUID portionId,
    String portionLabel,
    BigDecimal quantityGrams,
    String nutrientFocusCode,
    String reasonCode,
    String explanation,
    String sourceCode,
    String sourceFoodRef,
    List<PlanNutrientResponse> nutrients
) {

    public static DietPlanItemResponse from(DietPlanItem item) {
        return new DietPlanItemResponse(
            item.getId(),
            item.getMealType(),
            item.getDisplayOrder(),
            item.getFood().getSlug(),
            item.getFoodNameSnapshot(),
            item.getDietaryClassificationSnapshot(),
            item.getPortion() == null ? null : item.getPortion().getId(),
            item.getPortionLabelSnapshot(),
            item.getQuantityGrams(),
            item.getNutrientFocusCode(),
            item.getReasonCode(),
            item.getExplanation(),
            item.getSourceCodeSnapshot(),
            item.getSourceFoodRefSnapshot(),
            item.getNutrients().stream()
                .sorted(Comparator.comparing(DietPlanItemNutrient::getNutrientCode))
                .map(PlanNutrientResponse::from)
                .toList()
        );
    }
}

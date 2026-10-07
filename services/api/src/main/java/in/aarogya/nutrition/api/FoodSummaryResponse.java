package in.aarogya.nutrition.api;

import java.math.BigDecimal;
import java.util.Comparator;

import in.aarogya.nutrition.domain.Food;

public record FoodSummaryResponse(
    String slug,
    String name,
    String foodType,
    String category,
    String dietaryClassification,
    String primaryRegion,
    String nutrientStatus,
    boolean loggable,
    BigDecimal energyKcalPer100g,
    PortionResponse defaultPortion
) {

    public static FoodSummaryResponse from(Food food) {
        var energy = food.getNutrients().stream()
            .filter(nutrient -> "ENERGY_KCAL".equals(nutrient.getNutrientCode()))
            .map(nutrient -> nutrient.getAmountPer100g())
            .findFirst()
            .orElse(null);

        var defaultPortion = food.getPortions().stream()
            .sorted(Comparator.comparingInt(portion -> portion.getDisplayOrder()))
            .filter(portion -> portion.isDefaultPortion())
            .findFirst()
            .map(PortionResponse::from)
            .orElse(null);

        return new FoodSummaryResponse(
            food.getSlug(),
            food.getCanonicalName(),
            food.getFoodType(),
            food.getCategoryCode(),
            food.getDietaryClassification(),
            food.getPrimaryRegion(),
            food.getNutrientStatus(),
            "SOURCE_REFERENCED".equals(food.getNutrientStatus()) && energy != null,
            energy,
            defaultPortion
        );
    }
}

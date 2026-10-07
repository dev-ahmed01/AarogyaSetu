package in.aarogya.nutrition.api;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodIngredient;

public record FoodDetailResponse(
    String slug,
    String name,
    String description,
    String foodType,
    String category,
    String dietaryClassification,
    String primaryRegion,
    String nutrientStatus,
    boolean loggable,
    Set<String> aliases,
    Set<String> tags,
    Set<String> allergens,
    List<PortionResponse> portions,
    List<NutrientResponse> nutrients,
    List<IngredientResponse> ingredients,
    NutritionSourceResponse source,
    String sourceFoodRef
) {

    public static FoodDetailResponse from(
        Food food,
        List<FoodIngredient> ingredients
    ) {
        var portions = food.getPortions().stream()
            .sorted(Comparator.comparingInt(portion -> portion.getDisplayOrder()))
            .map(PortionResponse::from)
            .toList();

        var nutrients = food.getNutrients().stream()
            .sorted(Comparator.comparing(nutrient -> nutrient.getNutrientCode()))
            .map(NutrientResponse::from)
            .toList();

        return new FoodDetailResponse(
            food.getSlug(),
            food.getCanonicalName(),
            food.getDescription(),
            food.getFoodType(),
            food.getCategoryCode(),
            food.getDietaryClassification(),
            food.getPrimaryRegion(),
            food.getNutrientStatus(),
            "SOURCE_REFERENCED".equals(food.getNutrientStatus()) && !nutrients.isEmpty(),
            food.getAliases(),
            food.getTags(),
            food.getAllergens(),
            portions,
            nutrients,
            ingredients.stream().map(IngredientResponse::from).toList(),
            NutritionSourceResponse.from(food.getSource()),
            food.getSourceFoodRef()
        );
    }
}

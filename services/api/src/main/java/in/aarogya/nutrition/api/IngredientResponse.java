package in.aarogya.nutrition.api;

import java.math.BigDecimal;

import in.aarogya.nutrition.domain.FoodIngredient;

public record IngredientResponse(
    String slug,
    String name,
    BigDecimal quantityGrams
) {

    public static IngredientResponse from(FoodIngredient ingredient) {
        return new IngredientResponse(
            ingredient.getIngredientFood().getSlug(),
            ingredient.getIngredientFood().getCanonicalName(),
            ingredient.getQuantityGrams()
        );
    }
}

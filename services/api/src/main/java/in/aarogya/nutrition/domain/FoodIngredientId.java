package in.aarogya.nutrition.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class FoodIngredientId implements Serializable {

    @Column(name = "parent_food_id")
    private UUID parentFoodId;

    @Column(name = "ingredient_food_id")
    private UUID ingredientFoodId;

    protected FoodIngredientId() {
    }

    public FoodIngredientId(UUID parentFoodId, UUID ingredientFoodId) {
        this.parentFoodId = parentFoodId;
        this.ingredientFoodId = ingredientFoodId;
    }

    public UUID getParentFoodId() {
        return parentFoodId;
    }

    public UUID getIngredientFoodId() {
        return ingredientFoodId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof FoodIngredientId that)) return false;
        return Objects.equals(parentFoodId, that.parentFoodId)
            && Objects.equals(ingredientFoodId, that.ingredientFoodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentFoodId, ingredientFoodId);
    }
}

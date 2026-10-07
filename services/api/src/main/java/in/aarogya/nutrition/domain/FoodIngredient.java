package in.aarogya.nutrition.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_ingredients")
public class FoodIngredient {

    @EmbeddedId
    private FoodIngredientId id;

    @MapsId("parentFoodId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_food_id", nullable = false)
    private Food parentFood;

    @MapsId("ingredientFoodId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_food_id", nullable = false)
    private Food ingredientFood;

    @Column(name = "quantity_grams", precision = 8, scale = 2)
    private BigDecimal quantityGrams;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected FoodIngredient() {
    }

    public Food getParentFood() { return parentFood; }
    public Food getIngredientFood() { return ingredientFood; }
    public BigDecimal getQuantityGrams() { return quantityGrams; }
    public int getDisplayOrder() { return displayOrder; }
}

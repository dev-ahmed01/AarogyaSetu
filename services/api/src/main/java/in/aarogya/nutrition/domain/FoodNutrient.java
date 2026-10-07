package in.aarogya.nutrition.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_nutrients")
public class FoodNutrient {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "nutrient_code", nullable = false, length = 60)
    private String nutrientCode;

    @Column(name = "amount_per_100g", nullable = false, precision = 12, scale = 4)
    private BigDecimal amountPer100g;

    @Column(nullable = false, length = 20)
    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private NutritionSource source;

    @Column(name = "source_food_ref", length = 120)
    private String sourceFoodRef;

    protected FoodNutrient() {
    }

    public FoodNutrient(
        Food food,
        String nutrientCode,
        BigDecimal amountPer100g,
        String unit,
        NutritionSource source,
        String sourceFoodRef
    ) {
        this.id = UUID.randomUUID();
        this.food = food;
        this.nutrientCode = nutrientCode;
        this.amountPer100g = amountPer100g;
        this.unit = unit;
        this.source = source;
        this.sourceFoodRef = sourceFoodRef;
    }

    public UUID getId() { return id; }
    public String getNutrientCode() { return nutrientCode; }
    public BigDecimal getAmountPer100g() { return amountPer100g; }
    public String getUnit() { return unit; }
    public NutritionSource getSource() { return source; }
    public String getSourceFoodRef() { return sourceFoodRef; }

    public BigDecimal amountForGrams(BigDecimal grams) {
        return amountPer100g
            .multiply(grams)
            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }
}

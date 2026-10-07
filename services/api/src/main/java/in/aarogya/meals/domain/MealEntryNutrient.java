package in.aarogya.meals.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal_entry_nutrients")
public class MealEntryNutrient {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_entry_id", nullable = false)
    private MealEntry mealEntry;

    @Column(name = "nutrient_code", nullable = false, length = 60)
    private String nutrientCode;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String unit;

    protected MealEntryNutrient() {
    }

    MealEntryNutrient(
        MealEntry mealEntry,
        String nutrientCode,
        BigDecimal amount,
        String unit
    ) {
        this.id = UUID.randomUUID();
        this.mealEntry = mealEntry;
        this.nutrientCode = nutrientCode;
        this.amount = amount;
        this.unit = unit;
    }

    public String getNutrientCode() { return nutrientCode; }
    public BigDecimal getAmount() { return amount; }
    public String getUnit() { return unit; }
}

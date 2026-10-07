package in.aarogya.plans.domain;

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
@Table(name = "diet_plan_item_nutrients")
public class DietPlanItemNutrient {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_item_id", nullable = false)
    private DietPlanItem planItem;

    @Column(name = "nutrient_code", nullable = false, length = 60)
    private String nutrientCode;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String unit;

    protected DietPlanItemNutrient() {
    }

    DietPlanItemNutrient(
        DietPlanItem planItem,
        String nutrientCode,
        BigDecimal amount,
        String unit
    ) {
        this.id = UUID.randomUUID();
        this.planItem = planItem;
        this.nutrientCode = nutrientCode;
        this.amount = amount;
        this.unit = unit;
    }

    public String getNutrientCode() { return nutrientCode; }
    public BigDecimal getAmount() { return amount; }
    public String getUnit() { return unit; }
}

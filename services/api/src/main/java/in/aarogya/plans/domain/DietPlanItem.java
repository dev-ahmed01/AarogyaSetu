package in.aarogya.plans.domain;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodPortion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "diet_plan_items")
public class DietPlanItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private DietPlan plan;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "food_name_snapshot", nullable = false, length = 180)
    private String foodNameSnapshot;

    @Column(name = "dietary_classification_snapshot", nullable = false, length = 50)
    private String dietaryClassificationSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portion_id")
    private FoodPortion portion;

    @Column(name = "portion_label_snapshot", length = 100)
    private String portionLabelSnapshot;

    @Column(name = "quantity_grams", nullable = false, precision = 9, scale = 2)
    private BigDecimal quantityGrams;

    @Column(name = "nutrient_focus_code", length = 60)
    private String nutrientFocusCode;

    @Column(name = "reason_code", nullable = false, length = 100)
    private String reasonCode;

    @Column(nullable = false, length = 1000)
    private String explanation;

    @Column(name = "source_code_snapshot", length = 80)
    private String sourceCodeSnapshot;

    @Column(name = "source_food_ref_snapshot", length = 120)
    private String sourceFoodRefSnapshot;

    @OneToMany(
        mappedBy = "planItem",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<DietPlanItemNutrient> nutrients = new LinkedHashSet<>();

    protected DietPlanItem() {
    }

    public DietPlanItem(
        DietPlan plan,
        String mealType,
        int displayOrder,
        Food food,
        FoodPortion portion,
        BigDecimal quantityGrams,
        String nutrientFocusCode,
        String reasonCode,
        String explanation
    ) {
        this.id = UUID.randomUUID();
        this.plan = plan;
        this.mealType = mealType;
        this.displayOrder = displayOrder;
        this.food = food;
        this.foodNameSnapshot = food.getCanonicalName();
        this.dietaryClassificationSnapshot = food.getDietaryClassification();
        this.portion = portion;
        this.portionLabelSnapshot = portion == null ? "100 g" : portion.getLabel();
        this.quantityGrams = quantityGrams;
        this.nutrientFocusCode = nutrientFocusCode;
        this.reasonCode = reasonCode;
        this.explanation = explanation;
        this.sourceCodeSnapshot = food.getSource() == null
            ? null
            : food.getSource().getSourceCode();
        this.sourceFoodRefSnapshot = food.getSourceFoodRef();

        for (var nutrient : food.getNutrients()) {
            nutrients.add(new DietPlanItemNutrient(
                this,
                nutrient.getNutrientCode(),
                nutrient.amountForGrams(quantityGrams),
                nutrient.getUnit()
            ));
        }
    }

    public UUID getId() { return id; }
    public String getMealType() { return mealType; }
    public int getDisplayOrder() { return displayOrder; }
    public Food getFood() { return food; }
    public String getFoodNameSnapshot() { return foodNameSnapshot; }
    public String getDietaryClassificationSnapshot() { return dietaryClassificationSnapshot; }
    public FoodPortion getPortion() { return portion; }
    public String getPortionLabelSnapshot() { return portionLabelSnapshot; }
    public BigDecimal getQuantityGrams() { return quantityGrams; }
    public String getNutrientFocusCode() { return nutrientFocusCode; }
    public String getReasonCode() { return reasonCode; }
    public String getExplanation() { return explanation; }
    public String getSourceCodeSnapshot() { return sourceCodeSnapshot; }
    public String getSourceFoodRefSnapshot() { return sourceFoodRefSnapshot; }
    public Set<DietPlanItemNutrient> getNutrients() { return Set.copyOf(nutrients); }
}

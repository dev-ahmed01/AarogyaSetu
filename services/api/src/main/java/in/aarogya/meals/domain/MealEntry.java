package in.aarogya.meals.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodPortion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "meal_entries")
public class MealEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portion_id")
    private FoodPortion portion;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Column(name = "meal_type", nullable = false, length = 20)
    private String mealType;

    @Column(name = "quantity_grams", nullable = false, precision = 9, scale = 2)
    private BigDecimal quantityGrams;

    @Column(name = "portion_count", precision = 8, scale = 3)
    private BigDecimal portionCount;

    @Column(name = "food_name_snapshot", nullable = false, length = 180)
    private String foodNameSnapshot;

    @Column(name = "portion_label_snapshot", length = 100)
    private String portionLabelSnapshot;

    @Column(name = "source_code_snapshot", length = 80)
    private String sourceCodeSnapshot;

    @Column(name = "source_food_ref_snapshot", length = 120)
    private String sourceFoodRefSnapshot;

    @Column(name = "nutrient_status_snapshot", nullable = false, length = 50)
    private String nutrientStatusSnapshot;

    @Column(name = "dietary_classification_snapshot", nullable = false, length = 50)
    private String dietaryClassificationSnapshot;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "meal_entry_allergens",
        joinColumns = @JoinColumn(name = "meal_entry_id")
    )
    @Column(name = "allergen_code", nullable = false, length = 50)
    private Set<String> allergenSnapshots = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
        mappedBy = "mealEntry",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<MealEntryNutrient> nutrientSnapshots = new LinkedHashSet<>();

    protected MealEntry() {
    }

    public MealEntry(UserAccount user) {
        this.id = UUID.randomUUID();
        this.user = user;
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void replaceSnapshot(
        Food food,
        FoodPortion portion,
        LocalDate mealDate,
        String mealType,
        BigDecimal quantityGrams,
        BigDecimal portionCount,
        String portionLabel,
        String sourceCode,
        String sourceFoodRef
    ) {
        this.food = food;
        this.portion = portion;
        this.mealDate = mealDate;
        this.mealType = mealType;
        this.quantityGrams = quantityGrams;
        this.portionCount = portionCount;
        this.foodNameSnapshot = food.getCanonicalName();
        this.portionLabelSnapshot = portionLabel;
        this.sourceCodeSnapshot = sourceCode;
        this.sourceFoodRefSnapshot = sourceFoodRef;
        this.nutrientStatusSnapshot = food.getNutrientStatus();
        this.dietaryClassificationSnapshot = food.getDietaryClassification();
        this.allergenSnapshots.clear();
        this.allergenSnapshots.addAll(food.getAllergens());
        this.updatedAt = Instant.now();
        this.nutrientSnapshots.clear();

        for (var nutrient : food.getNutrients()) {
            this.nutrientSnapshots.add(new MealEntryNutrient(
                this,
                nutrient.getNutrientCode(),
                nutrient.amountForGrams(quantityGrams),
                nutrient.getUnit()
            ));
        }
    }

    public UUID getId() { return id; }
    public UserAccount getUser() { return user; }
    public Food getFood() { return food; }
    public FoodPortion getPortion() { return portion; }
    public LocalDate getMealDate() { return mealDate; }
    public String getMealType() { return mealType; }
    public BigDecimal getQuantityGrams() { return quantityGrams; }
    public BigDecimal getPortionCount() { return portionCount; }
    public String getFoodNameSnapshot() { return foodNameSnapshot; }
    public String getPortionLabelSnapshot() { return portionLabelSnapshot; }
    public String getSourceCodeSnapshot() { return sourceCodeSnapshot; }
    public String getSourceFoodRefSnapshot() { return sourceFoodRefSnapshot; }
    public String getNutrientStatusSnapshot() { return nutrientStatusSnapshot; }
    public String getDietaryClassificationSnapshot() {
        return dietaryClassificationSnapshot;
    }
    public Set<String> getAllergenSnapshots() {
        return Set.copyOf(allergenSnapshots);
    }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<MealEntryNutrient> getNutrientSnapshots() {
        return Set.copyOf(nutrientSnapshots);
    }
}

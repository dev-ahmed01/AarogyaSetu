package in.aarogya.nutrition.domain;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

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
@Table(name = "foods")
public class Food {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 140)
    private String slug;

    @Column(name = "canonical_name", nullable = false, length = 180)
    private String canonicalName;

    @Column(length = 1000)
    private String description;

    @Column(name = "food_type", nullable = false, length = 40)
    private String foodType;

    @Column(name = "category_code", nullable = false, length = 50)
    private String categoryCode;

    @Column(name = "dietary_classification", nullable = false, length = 50)
    private String dietaryClassification;

    @Column(name = "primary_region", length = 80)
    private String primaryRegion;

    @Column(name = "nutrient_status", nullable = false, length = 50)
    private String nutrientStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private NutritionSource source;

    @Column(name = "source_food_ref", length = 120)
    private String sourceFoodRef;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "food_aliases", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "alias", nullable = false, length = 180)
    private Set<String> aliases = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "food_tags", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "tag_code", nullable = false, length = 80)
    private Set<String> tags = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "food_allergens", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "allergen_code", nullable = false, length = 50)
    private Set<String> allergens = new LinkedHashSet<>();

    @OneToMany(mappedBy = "food")
    private Set<FoodPortion> portions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "food")
    private Set<FoodNutrient> nutrients = new LinkedHashSet<>();

    protected Food() {
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

    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getCanonicalName() { return canonicalName; }
    public String getDescription() { return description; }
    public String getFoodType() { return foodType; }
    public String getCategoryCode() { return categoryCode; }
    public String getDietaryClassification() { return dietaryClassification; }
    public String getPrimaryRegion() { return primaryRegion; }
    public String getNutrientStatus() { return nutrientStatus; }
    public NutritionSource getSource() { return source; }
    public String getSourceFoodRef() { return sourceFoodRef; }
    public boolean isActive() { return active; }
    public Set<String> getAliases() { return Set.copyOf(aliases); }
    public Set<String> getTags() { return Set.copyOf(tags); }
    public Set<String> getAllergens() { return Set.copyOf(allergens); }
    public Set<FoodPortion> getPortions() { return Set.copyOf(portions); }
    public Set<FoodNutrient> getNutrients() { return Set.copyOf(nutrients); }
}

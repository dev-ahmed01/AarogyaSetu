package in.aarogya.profile.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import in.aarogya.identity.domain.UserAccount;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "health_profiles")
public class HealthProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(name = "age_years")
    private Integer ageYears;

    @Column(name = "sex_for_nutrition", length = 30)
    private String sexForNutrition;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "activity_level", length = 30)
    private String activityLevel;

    @Column(name = "dietary_pattern", length = 40)
    private String dietaryPattern;

    @Column(name = "state_or_region", length = 80)
    private String stateOrRegion;

    @Column(name = "onboarding_completed_at")
    private Instant onboardingCompletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_goals", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "goal_code", nullable = false, length = 50)
    private Set<String> goals = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_allergies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "allergy_code", nullable = false, length = 50)
    private Set<String> allergies = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_health_contexts", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "context_code", nullable = false, length = 60)
    private Set<String> healthContexts = new LinkedHashSet<>();

    protected HealthProfile() {
    }

    public HealthProfile(UserAccount user) {
        this.user = user;
        this.userId = user.getId();
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void update(
        Integer ageYears,
        String sexForNutrition,
        BigDecimal heightCm,
        BigDecimal weightKg,
        String activityLevel,
        String dietaryPattern,
        String stateOrRegion,
        Set<String> goals,
        Set<String> allergies,
        Set<String> healthContexts
    ) {
        this.ageYears = ageYears;
        this.sexForNutrition = sexForNutrition;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.activityLevel = activityLevel;
        this.dietaryPattern = dietaryPattern;
        this.stateOrRegion = normalizeOptional(stateOrRegion);
        this.goals = new LinkedHashSet<>(goals);
        this.allergies = new LinkedHashSet<>(allergies);
        this.healthContexts = new LinkedHashSet<>(healthContexts);
    }

    public void markOnboardingComplete() {
        this.onboardingCompletedAt = Instant.now();
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        var trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public UUID getUserId() { return userId; }
    public Integer getAgeYears() { return ageYears; }
    public String getSexForNutrition() { return sexForNutrition; }
    public BigDecimal getHeightCm() { return heightCm; }
    public BigDecimal getWeightKg() { return weightKg; }
    public String getActivityLevel() { return activityLevel; }
    public String getDietaryPattern() { return dietaryPattern; }
    public String getStateOrRegion() { return stateOrRegion; }
    public Instant getOnboardingCompletedAt() { return onboardingCompletedAt; }
    public Set<String> getGoals() { return Set.copyOf(goals); }
    public Set<String> getAllergies() { return Set.copyOf(allergies); }
    public Set<String> getHealthContexts() { return Set.copyOf(healthContexts); }
}

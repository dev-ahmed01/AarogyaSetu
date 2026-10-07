package in.aarogya.recommendations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.meals.api.DailyMealLogResponse;
import in.aarogya.meals.api.MealEntryResponse;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.recommendations.service.RecommendationPolicy;

class RecommendationPolicyTests {

    private final RecommendationPolicy policy = new RecommendationPolicy();

    @Test
    void trendDayNeedsThreeEntriesAcrossTwoMealSlots() {
        var eligible = new DailyMealLogResponse(
            LocalDate.of(2026, 10, 6),
            3,
            List.of(
                entry("BREAKFAST", Set.of()),
                entry("LUNCH", Set.of()),
                entry("LUNCH", Set.of())
            ),
            List.of()
        );

        var oneMealSlot = new DailyMealLogResponse(
            LocalDate.of(2026, 10, 5),
            3,
            List.of(
                entry("DINNER", Set.of()),
                entry("DINNER", Set.of()),
                entry("DINNER", Set.of())
            ),
            List.of()
        );

        assertTrue(policy.isTrendDayEligible(eligible));
        assertFalse(policy.isTrendDayEligible(oneMealSlot));
    }

    @Test
    void adultProteinReferenceUsesWeightAndRuleReference() {
        assertEquals(
            new BigDecimal("58.10"),
            policy.adultProteinReference(
                new BigDecimal("70"),
                new BigDecimal("0.83")
            )
        );
    }

    @Test
    void dietCompatibilityIsConservative() {
        assertTrue(policy.isDietCompatible("VEGETARIAN", "VEGAN"));
        assertTrue(policy.isDietCompatible("EGGETARIAN", "VEGETARIAN"));
        assertFalse(policy.isDietCompatible("VEGAN", "VEGETARIAN"));
        assertFalse(policy.isDietCompatible("VEGETARIAN", "EGGETARIAN"));
    }

    @Test
    void allergenConflictsUseSnapshotFlags() {
        var conflicts = policy.allergenConflicts(
            Set.of("PEANUT", "MILK"),
            entry("SNACK", Set.of("PEANUT"))
        );

        assertEquals(Set.of("PEANUT"), conflicts);
    }

    @Test
    void kidneyContextSuppressesGenericProteinRule() {
        var profile = new ProfileResponse(
            24,
            "MALE",
            new BigDecimal("175"),
            new BigDecimal("70"),
            "MODERATE",
            "VEGETARIAN",
            "Karnataka",
            Set.of("BALANCED_NUTRITION"),
            Set.of(),
            Set.of("KIDNEY_CONDITION"),
            true,
            "2026-10",
            java.time.Instant.now(),
            true,
            java.time.Instant.now()
        );

        assertTrue(policy.suppressGenericProteinRule(profile));
    }

    private MealEntryResponse entry(String mealType, Set<String> allergens) {
        return new MealEntryResponse(
            UUID.randomUUID(),
            "test-food",
            "Test food",
            LocalDate.of(2026, 10, 6),
            mealType,
            new BigDecimal("100"),
            null,
            "Custom amount",
            null,
            "USDA_FDC",
            "TEST",
            "SOURCE_REFERENCED",
            "VEGAN",
            allergens,
            List.of(),
            java.time.Instant.now(),
            java.time.Instant.now()
        );
    }
}

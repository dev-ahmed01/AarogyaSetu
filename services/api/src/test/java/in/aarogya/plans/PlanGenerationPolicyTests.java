package in.aarogya.plans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodNutrient;
import in.aarogya.plans.service.PlanGenerationPolicy;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.recommendations.api.RecommendationAssessmentResponse;
import in.aarogya.recommendations.api.RecommendationItemResponse;
import in.aarogya.recommendations.service.RecommendationPolicy;

class PlanGenerationPolicyTests {

    private final PlanGenerationPolicy policy =
        new PlanGenerationPolicy(new RecommendationPolicy());

    @Test
    void eligibleFoodMustPassDietAndAllergyFilters() {
        var food = mock(Food.class);
        var nutrient = mock(FoodNutrient.class);

        when(food.isActive()).thenReturn(true);
        when(food.getNutrientStatus()).thenReturn("SOURCE_REFERENCED");
        when(food.getNutrients()).thenReturn(Set.of(nutrient));
        when(food.getDietaryClassification()).thenReturn("VEGAN");
        when(food.getAllergens()).thenReturn(Set.of("PEANUT"));

        assertFalse(policy.isEligibleFood(
            profile("VEGAN", Set.of("PEANUT")),
            food
        ));

        assertTrue(policy.isEligibleFood(
            profile("VEGAN", Set.of()),
            food
        ));

        when(food.getDietaryClassification()).thenReturn("VEGETARIAN");

        assertFalse(policy.isEligibleFood(
            profile("VEGAN", Set.of()),
            food
        ));
    }

    @Test
    void fibreRecommendationBecomesFibreFocus() {
        var assessment = new RecommendationAssessmentResponse(
            "READY",
            LocalDate.of(2026, 10, 7),
            LocalDate.of(2026, 9, 30),
            LocalDate.of(2026, 10, 6),
            7,
            3,
            2,
            List.of(new RecommendationItemResponse(
                "FIBRE_TREND_LOW",
                1,
                1,
                "Fibre has been below the reference level",
                "FIBRE_BELOW_REFERENCE_TREND",
                "WELLNESS",
                "Observed fibre was low.",
                "Reference explanation.",
                "Consider fibre-rich foods.",
                new BigDecimal("15"),
                new BigDecimal("25"),
                "g/day",
                null
            )),
            List.of()
        );

        var focus = policy.focusFrom(assessment);

        assertEquals("FIBRE_FOCUS", focus.mode());
        assertEquals("FIBRE_G", focus.nutrientCode());
        assertEquals("FIBRE_TREND_LOW", focus.sourceRuleCode());
    }

    private ProfileResponse profile(
        String dietaryPattern,
        Set<String> allergies
    ) {
        return new ProfileResponse(
            24,
            null,
            null,
            new BigDecimal("70"),
            "MODERATE",
            dietaryPattern,
            "Karnataka",
            Set.of("BALANCED_NUTRITION"),
            allergies,
            Set.of(),
            true,
            "2026-10",
            Instant.now(),
            true,
            Instant.now()
        );
    }
}

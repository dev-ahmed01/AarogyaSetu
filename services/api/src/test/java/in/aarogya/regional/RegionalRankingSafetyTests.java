package in.aarogya.regional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;

import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodNutrient;
import in.aarogya.plans.service.PlanGenerationPolicy;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.recommendations.service.RecommendationPolicy;
import in.aarogya.regional.domain.FoodRegionalAffinity;
import in.aarogya.regional.service.RegionalIntelligenceService;

class RegionalRankingSafetyTests {

    @Test
    void regionalBonusIsCappedAndCannotCreateEligibility() {
        var affinity = mock(FoodRegionalAffinity.class);
        when(affinity.getRelationship()).thenReturn("REGIONAL_FAMILIAR");

        var service = new RegionalIntelligenceService(
            null, null, null, null, null, null
        );
        var fit = new RegionalIntelligenceService.RegionalFit(
            affinity,
            100
        );

        assertEquals(12.0, service.planningBonus(fit));

        var policy = new PlanGenerationPolicy(new RecommendationPolicy());
        var food = mock(Food.class);
        var nutrient = mock(FoodNutrient.class);

        when(food.isActive()).thenReturn(true);
        when(food.getNutrientStatus()).thenReturn("SOURCE_REFERENCED");
        when(food.getNutrients()).thenReturn(Set.of(nutrient));
        when(food.getDietaryClassification()).thenReturn("VEGAN");
        when(food.getAllergens()).thenReturn(Set.of("PEANUT"));

        var profile = new ProfileResponse(
            24, null, null, null, "MODERATE", "VEGAN", "Karnataka",
            Set.of("BALANCED_NUTRITION"), Set.of("PEANUT"), Set.of(),
            true, "2026-10", Instant.now(), true, Instant.now()
        );

        assertFalse(policy.isEligibleFood(profile, food));
    }
}

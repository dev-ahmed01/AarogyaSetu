package in.aarogya.plans;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.nutrition.repository.FoodRepository;
import in.aarogya.plans.repository.DietPlanRepository;
import in.aarogya.plans.service.DietPlanService;
import in.aarogya.plans.service.PlanGenerationPolicy;
import in.aarogya.plans.service.PlanGenerationUnavailableException;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.profile.service.ProfileService;
import in.aarogya.recommendations.service.RecommendationEngineService;
import in.aarogya.recommendations.service.RecommendationPolicy;
import in.aarogya.regional.service.RegionalIntelligenceService;

class DietPlanServiceTests {

    @Test
    void pausedConsentStopsBeforeRecommendationOrFoodRanking() {
        var plans = mock(DietPlanRepository.class);
        var foods = mock(FoodRepository.class);
        var users = mock(UserAccountRepository.class);
        var profiles = mock(ProfileService.class);
        var recommendations = mock(RecommendationEngineService.class);
        var regional = mock(RegionalIntelligenceService.class);

        var service = new DietPlanService(
            plans,
            foods,
            users,
            profiles,
            recommendations,
            new PlanGenerationPolicy(new RecommendationPolicy()),
            regional
        );

        var userId = UUID.randomUUID();
        when(profiles.getProfile(userId)).thenReturn(new ProfileResponse(
            24,
            null,
            null,
            null,
            "MODERATE",
            "VEGETARIAN",
            null,
            Set.of("BALANCED_NUTRITION"),
            Set.of(),
            Set.of(),
            false,
            "2026-10",
            Instant.now(),
            true,
            Instant.now()
        ));

        assertThrows(
            PlanGenerationUnavailableException.class,
            () -> service.suggestions(
                userId,
                LocalDate.of(2026, 10, 7),
                6
            )
        );

        verifyNoInteractions(recommendations, foods, plans, users, regional);
    }
}

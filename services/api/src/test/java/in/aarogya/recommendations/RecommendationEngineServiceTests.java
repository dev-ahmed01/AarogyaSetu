package in.aarogya.recommendations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.meals.service.MealLogService;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.profile.service.ProfileService;
import in.aarogya.recommendations.repository.RecommendationRuleRepository;
import in.aarogya.recommendations.service.RecommendationEngineService;
import in.aarogya.recommendations.service.RecommendationPolicy;

class RecommendationEngineServiceTests {

    @Test
    void pausedConsentStopsBeforeMealAnalysis() {
        var profiles = mock(ProfileService.class);
        var meals = mock(MealLogService.class);
        var rules = mock(RecommendationRuleRepository.class);
        var service = new RecommendationEngineService(
            profiles,
            meals,
            rules,
            new RecommendationPolicy()
        );

        var userId = UUID.randomUUID();
        when(profiles.getProfile(userId)).thenReturn(new ProfileResponse(
            22,
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
            java.time.Instant.now(),
            true,
            java.time.Instant.now()
        ));

        var result = service.assess(
            userId,
            java.time.LocalDate.of(2026, 10, 7)
        );

        assertEquals("PERSONALIZATION_PAUSED", result.status());
        assertEquals(0, result.recommendations().size());
        verifyNoInteractions(meals, rules);
    }
}

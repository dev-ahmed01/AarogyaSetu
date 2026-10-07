package in.aarogya.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.analytics.service.LongitudinalAnalyticsService;
import in.aarogya.health.api.HealthConsentResponse;
import in.aarogya.health.repository.HealthObservationRepository;
import in.aarogya.health.service.HealthRecordService;
import in.aarogya.meals.domain.MealEntry;
import in.aarogya.meals.domain.MealEntryNutrient;
import in.aarogya.meals.repository.MealEntryRepository;

class LongitudinalAnalyticsServiceTests {

    @Test
    void unloggedDaysRemainMissingAndAveragesUseObservedDays() {
        var meals = mock(MealEntryRepository.class);
        var observations = mock(HealthObservationRepository.class);
        var health = mock(HealthRecordService.class);
        var userId = UUID.randomUUID();
        var asOf = LocalDate.of(2026, 10, 7);

        var currentOne = entry(
            LocalDate.of(2026, 10, 6),
            "LUNCH",
            "1000",
            "40",
            "15"
        );
        var currentTwo = entry(
            LocalDate.of(2026, 10, 7),
            "DINNER",
            "2000",
            "60",
            "25"
        );
        var previousOne = entry(
            LocalDate.of(2026, 9, 29),
            "LUNCH",
            "900",
            "35",
            "12"
        );
        var previousTwo = entry(
            LocalDate.of(2026, 9, 30),
            "DINNER",
            "1100",
            "45",
            "18"
        );

        when(meals.findByUser_IdAndMealDateBetweenOrderByMealDateDescCreatedAtAsc(
            userId,
            LocalDate.of(2026, 9, 24),
            asOf
        )).thenReturn(List.of(
            currentOne,
            currentTwo,
            previousOne,
            previousTwo
        ));

        when(health.consent(userId)).thenReturn(
            new HealthConsentResponse(
                "HEALTH_RECORD_ANALYSIS",
                false,
                null,
                null
            )
        );

        var service = new LongitudinalAnalyticsService(
            meals,
            observations,
            health
        );

        var result = service.analyze(userId, asOf, 7);

        assertEquals(2, result.coverage().loggedDays());
        assertEquals(29, result.coverage().coveragePercent());
        assertEquals(7, result.dailyNutrition().size());
        assertEquals(LocalDate.of(2026, 10, 1), result.dailyNutrition().get(0).date());
        assertEquals(false, result.dailyNutrition().get(0).logged());
        assertNull(result.dailyNutrition().get(0).energyKcal());

        var energy = result.nutrientTrends().stream()
            .filter(item -> "ENERGY_KCAL".equals(item.nutrientCode()))
            .findFirst()
            .orElseThrow();

        assertEquals(new BigDecimal("1500.00"), energy.currentLoggedDayAverage());
        assertEquals(new BigDecimal("1000.00"), energy.previousLoggedDayAverage());
        assertEquals(new BigDecimal("50.0"), energy.changePercent());
        assertEquals("HIGHER", energy.direction());

        verifyNoInteractions(observations);
    }

    @Test
    void consentedHealthTrendQueriesManualBodyWeightOnly() {
        var meals = mock(MealEntryRepository.class);
        var observations = mock(HealthObservationRepository.class);
        var health = mock(HealthRecordService.class);
        var userId = UUID.randomUUID();
        var asOf = LocalDate.of(2026, 10, 7);

        when(meals.findByUser_IdAndMealDateBetweenOrderByMealDateDescCreatedAtAsc(
            userId,
            LocalDate.of(2026, 9, 24),
            asOf
        )).thenReturn(List.of());

        when(health.consent(userId)).thenReturn(
            new HealthConsentResponse(
                "HEALTH_RECORD_ANALYSIS",
                true,
                "2026-10-health-v1",
                null
            )
        );

        when(observations
            .findByHealthRecord_User_IdAndHealthRecord_SourceTypeAndObservationCodeAndValueNumericIsNotNullOrderByObservedAtAsc(
                userId,
                "MANUAL",
                "BODY_WEIGHT"
            )).thenReturn(List.of());

        var service = new LongitudinalAnalyticsService(
            meals,
            observations,
            health
        );

        var result = service.analyze(userId, asOf, 7);

        assertEquals("NO_DATA", result.healthTrend().status());
        verify(observations)
            .findByHealthRecord_User_IdAndHealthRecord_SourceTypeAndObservationCodeAndValueNumericIsNotNullOrderByObservedAtAsc(
                userId,
                "MANUAL",
                "BODY_WEIGHT"
            );
    }

    @Test
    void unsupportedWindowIsRejected() {
        var service = new LongitudinalAnalyticsService(
            mock(MealEntryRepository.class),
            mock(HealthObservationRepository.class),
            mock(HealthRecordService.class)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.analyze(
                UUID.randomUUID(),
                LocalDate.of(2026, 10, 7),
                14
            )
        );
    }

    private MealEntry entry(
        LocalDate date,
        String mealType,
        String energy,
        String protein,
        String fibre
    ) {
        var entry = mock(MealEntry.class);

        when(entry.getMealDate()).thenReturn(date);
        when(entry.getMealType()).thenReturn(mealType);
        when(entry.getNutrientSnapshots()).thenReturn(Set.of(
            nutrient("ENERGY_KCAL", energy, "kcal"),
            nutrient("PROTEIN_G", protein, "g"),
            nutrient("FIBRE_G", fibre, "g")
        ));

        return entry;
    }

    private MealEntryNutrient nutrient(
        String code,
        String amount,
        String unit
    ) {
        var nutrient = mock(MealEntryNutrient.class);
        when(nutrient.getNutrientCode()).thenReturn(code);
        when(nutrient.getAmount()).thenReturn(new BigDecimal(amount));
        when(nutrient.getUnit()).thenReturn(unit);
        return nutrient;
    }
}

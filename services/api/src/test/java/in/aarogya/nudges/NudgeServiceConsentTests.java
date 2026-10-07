package in.aarogya.nudges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.health.api.HealthConsentResponse;
import in.aarogya.health.repository.HealthObservationRepository;
import in.aarogya.health.service.HealthRecordService;
import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.nudges.repository.NudgeInstanceRepository;
import in.aarogya.nudges.repository.NudgeRuleRepository;
import in.aarogya.nudges.service.NudgeService;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.profile.service.ProfileService;
import in.aarogya.recommendations.api.RecommendationAssessmentResponse;
import in.aarogya.recommendations.service.RecommendationEngineService;
import in.aarogya.security.SecurityAuditService;

class NudgeServiceConsentTests {

    @Test
    void healthRecordQueryIsSkippedWhenAnalysisConsentIsOff() {
        var fixture = fixture(false);

        var result = fixture.service.evaluate(
            fixture.userId,
            LocalDate.of(2026, 10, 7)
        );

        assertEquals(0, result.signalsEvaluated());
        verifyNoInteractions(fixture.observations);
    }

    @Test
    void healthRecordPersonalizationQueriesManualSourceOnly() {
        var fixture = fixture(true);

        fixture.service.evaluate(
            fixture.userId,
            LocalDate.of(2026, 10, 7)
        );

        verify(fixture.observations)
            .findFirstByHealthRecord_User_IdAndHealthRecord_SourceTypeAndObservationCodeAndValueNumericIsNotNullOrderByObservedAtDesc(
                fixture.userId,
                "MANUAL",
                "BODY_WEIGHT"
            );
    }

    private Fixture fixture(boolean healthConsent) {
        var rules = mock(NudgeRuleRepository.class);
        var instances = mock(NudgeInstanceRepository.class);
        var recommendations = mock(RecommendationEngineService.class);
        var profiles = mock(ProfileService.class);
        var healthRecords = mock(HealthRecordService.class);
        var observations = mock(HealthObservationRepository.class);
        var users = mock(UserAccountRepository.class);
        var audit = mock(SecurityAuditService.class);

        var userId = UUID.randomUUID();
        when(users.findById(userId)).thenReturn(Optional.of(mock(UserAccount.class)));
        when(rules.findByActiveTrueOrderByRuleCodeAscRuleVersionDesc())
            .thenReturn(List.of());
        when(instances.findByUser_IdAndStatusInOrderByLastEvaluatedAtDesc(
            userId,
            List.of("ACTIVE", "SNOOZED")
        )).thenReturn(List.of());
        when(instances.findByUser_IdAndStatusInOrderByLastEvaluatedAtDesc(
            userId,
            List.of("ACTIVE")
        )).thenReturn(List.of());

        when(recommendations.assess(
            userId,
            LocalDate.of(2026, 10, 7)
        )).thenReturn(new RecommendationAssessmentResponse(
            "READY",
            LocalDate.of(2026, 10, 7),
            LocalDate.of(2026, 9, 30),
            LocalDate.of(2026, 10, 6),
            7,
            2,
            2,
            List.of(),
            List.of()
        ));

        when(profiles.getProfile(userId)).thenReturn(new ProfileResponse(
            24,
            null,
            null,
            new BigDecimal("70.0"),
            "MODERATE",
            "VEGETARIAN",
            "Karnataka",
            Set.of("BALANCED_NUTRITION"),
            Set.of(),
            Set.of(),
            true,
            "2026-10",
            Instant.now(),
            true,
            Instant.now()
        ));

        when(healthRecords.consent(userId)).thenReturn(
            new HealthConsentResponse(
                "HEALTH_RECORD_ANALYSIS",
                healthConsent,
                healthConsent ? "2026-10-health-v1" : null,
                healthConsent ? Instant.now() : null
            )
        );

        when(observations
            .findFirstByHealthRecord_User_IdAndHealthRecord_SourceTypeAndObservationCodeAndValueNumericIsNotNullOrderByObservedAtDesc(
                userId,
                "MANUAL",
                "BODY_WEIGHT"
            )).thenReturn(Optional.empty());

        return new Fixture(
            userId,
            observations,
            new NudgeService(
                rules,
                instances,
                recommendations,
                profiles,
                healthRecords,
                observations,
                users,
                audit
            )
        );
    }

    private record Fixture(
        UUID userId,
        HealthObservationRepository observations,
        NudgeService service
    ) {
    }
}

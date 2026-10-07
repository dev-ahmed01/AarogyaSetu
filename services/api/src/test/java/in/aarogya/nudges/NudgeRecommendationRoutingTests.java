package in.aarogya.nudges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import in.aarogya.health.api.HealthConsentResponse;
import in.aarogya.health.repository.HealthObservationRepository;
import in.aarogya.health.service.HealthRecordService;
import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.nudges.domain.NudgeInstance;
import in.aarogya.nudges.domain.NudgeRule;
import in.aarogya.nudges.repository.NudgeInstanceRepository;
import in.aarogya.nudges.repository.NudgeRuleRepository;
import in.aarogya.nudges.service.NudgeService;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.profile.service.ProfileService;
import in.aarogya.recommendations.api.EvidenceSourceResponse;
import in.aarogya.recommendations.api.RecommendationAssessmentResponse;
import in.aarogya.recommendations.api.RecommendationItemResponse;
import in.aarogya.recommendations.service.RecommendationEngineService;
import in.aarogya.security.SecurityAuditService;

class NudgeRecommendationRoutingTests {

    @Test
    void allergySafetyRecommendationBecomesAttentionNudge() {
        var rules = mock(NudgeRuleRepository.class);
        var instances = mock(NudgeInstanceRepository.class);
        var recommendations = mock(RecommendationEngineService.class);
        var profiles = mock(ProfileService.class);
        var healthRecords = mock(HealthRecordService.class);
        var observations = mock(HealthObservationRepository.class);
        var users = mock(UserAccountRepository.class);
        var audit = mock(SecurityAuditService.class);
        var user = mock(UserAccount.class);
        var userId = UUID.randomUUID();
        var date = LocalDate.of(2026, 10, 7);

        var rule = mock(NudgeRule.class);
        when(rule.getRuleCode()).thenReturn("ALLERGEN_CONFLICT_NUDGE");
        when(rule.getRuleVersion()).thenReturn(1);
        when(rule.getCategory()).thenReturn("SAFETY");
        when(rule.getSeverity()).thenReturn("ATTENTION");
        when(rule.getCooldownHours()).thenReturn(24);
        when(rules.findByActiveTrueOrderByRuleCodeAscRuleVersionDesc())
            .thenReturn(List.of(rule));

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(recommendations.assess(userId, date)).thenReturn(
            new RecommendationAssessmentResponse(
                "READY_WITH_SAFETY_ATTENTION",
                date,
                date.minusDays(7),
                date.minusDays(1),
                7,
                2,
                2,
                List.of(new RecommendationItemResponse(
                    "ALLERGEN_CONFLICT",
                    1,
                    0,
                    "A logged food conflicts with a recorded allergy",
                    "LOGGED_ALLERGEN_CONFLICT",
                    "SAFETY_ATTENTION",
                    "A logged food is flagged with PEANUT.",
                    "Catalog/profile consistency warning.",
                    "Review the food and its label.",
                    null,
                    null,
                    null,
                    new EvidenceSourceResponse(
                        "FSSAI_ALLERGEN_REFERENCE",
                        "FSSAI allergen reference",
                        "regulatory reference",
                        "https://www.fssai.gov.in/"
                    )
                )),
                List.of()
            )
        );

        when(profiles.getProfile(userId)).thenReturn(new ProfileResponse(
            24,
            null,
            null,
            null,
            "MODERATE",
            "VEGETARIAN",
            "Karnataka",
            Set.of("BALANCED_NUTRITION"),
            Set.of("PEANUT"),
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
                false,
                null,
                null
            )
        );
        when(instances.findByUser_IdAndNudgeKey(
            userId,
            "RULE:ALLERGEN_CONFLICT_NUDGE"
        )).thenReturn(Optional.empty());
        when(instances.findByUser_IdAndStatusInOrderByLastEvaluatedAtDesc(
            userId,
            List.of("ACTIVE", "SNOOZED")
        )).thenReturn(List.of());
        when(instances.findByUser_IdAndStatusInOrderByLastEvaluatedAtDesc(
            userId,
            List.of("ACTIVE")
        )).thenReturn(List.of());

        var service = new NudgeService(
            rules,
            instances,
            recommendations,
            profiles,
            healthRecords,
            observations,
            users,
            audit
        );

        var result = service.evaluate(userId, date);

        assertEquals(1, result.signalsEvaluated());

        var captor = ArgumentCaptor.forClass(NudgeInstance.class);
        verify(instances).save(captor.capture());
        assertEquals(
            "ALLERGEN_CONFLICT_NUDGE",
            captor.getValue().getRuleCode()
        );
        assertEquals("ATTENTION", captor.getValue().getSeverity());
        assertEquals("RECOMMENDATION", captor.getValue().getSourceType());
    }
}

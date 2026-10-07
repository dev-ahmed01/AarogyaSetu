package in.aarogya.research;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.meals.repository.MealEntryRepository;
import in.aarogya.profile.domain.ConsentRecord;
import in.aarogya.profile.repository.ConsentRecordRepository;
import in.aarogya.profile.repository.HealthProfileRepository;
import in.aarogya.research.api.ResearchConsentUpdateRequest;
import in.aarogya.research.repository.ResearchFeatureEventRepository;
import in.aarogya.research.repository.ResearchMetricDefinitionRepository;
import in.aarogya.research.service.ResearchEvaluationService;
import in.aarogya.security.SecurityAuditService;

class ResearchEvaluationServiceTests {

    @Test
    void featureEventIsNotStoredWithoutResearchConsent() {
        var fixture = fixture();
        var userId = UUID.randomUUID();

        when(fixture.consents
            .findTopByUser_IdAndConsentTypeOrderByRecordedAtDesc(
                userId,
                ResearchEvaluationService.RESEARCH_CONSENT
            ))
            .thenReturn(Optional.empty());

        var result = fixture.service.recordEvent(
            userId,
            "GUIDANCE_VIEWED"
        );

        assertFalse(result.recorded());
        verify(fixture.events, never()).save(any());
    }

    @Test
    void revokingResearchConsentDeletesInstrumentation() {
        var fixture = fixture();
        var userId = UUID.randomUUID();
        var user = mock(UserAccount.class);

        when(fixture.users.findById(userId))
            .thenReturn(Optional.of(user));
        when(user.getId()).thenReturn(userId);
        when(fixture.consents.save(any(ConsentRecord.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var result = fixture.service.updateConsent(
            userId,
            new ResearchConsentUpdateRequest(
                false,
                ResearchEvaluationService.RESEARCH_POLICY_VERSION
            )
        );

        assertFalse(result.granted());
        verify(fixture.events).deleteByUser_Id(userId);
    }

    @Test
    void cohortsBelowFiveParticipantsAreSuppressed() {
        var fixture = fixture();
        var consentHistory = new ArrayList<ConsentRecord>();

        for (int index = 0; index < 4; index++) {
            var user = mock(UserAccount.class);
            var consent = mock(ConsentRecord.class);
            var userId = UUID.randomUUID();

            when(user.getId()).thenReturn(userId);
            when(consent.getUser()).thenReturn(user);
            when(consent.isGranted()).thenReturn(true);
            when(consent.getRecordedAt())
                .thenReturn(java.time.Instant.parse(
                    "2026-09-01T00:00:00Z"
                ));

            consentHistory.add(consent);
        }

        when(fixture.consents
            .findByConsentTypeOrderByRecordedAtAsc(
                ResearchEvaluationService.RESEARCH_CONSENT
            ))
            .thenReturn(consentHistory);

        when(fixture.events
            .findByUser_IdInAndEventDateBetweenOrderByEventDateAsc(
                anyCollection(),
                any(LocalDate.class),
                any(LocalDate.class)
            ))
            .thenReturn(List.of());

        when(fixture.meals
            .findByUser_IdInAndMealDateBetweenOrderByMealDateAscCreatedAtAsc(
                anyCollection(),
                any(LocalDate.class),
                any(LocalDate.class)
            ))
            .thenReturn(List.of());

        when(fixture.profiles.findByUserIdIn(anyCollection()))
            .thenReturn(List.of());

        var overview = fixture.service.overview(
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 30)
        );

        assertTrue(
            overview.metrics().stream()
                .allMatch(metric -> metric.suppressed())
        );
    }

    private Fixture fixture() {
        var consents = mock(ConsentRecordRepository.class);
        var events = mock(ResearchFeatureEventRepository.class);
        var definitions = mock(ResearchMetricDefinitionRepository.class);
        var meals = mock(MealEntryRepository.class);
        var profiles = mock(HealthProfileRepository.class);
        var users = mock(UserAccountRepository.class);
        var audit = mock(SecurityAuditService.class);

        return new Fixture(
            new ResearchEvaluationService(
                consents,
                events,
                definitions,
                meals,
                profiles,
                users,
                audit
            ),
            consents,
            events,
            meals,
            profiles,
            users
        );
    }

    private record Fixture(
        ResearchEvaluationService service,
        ConsentRecordRepository consents,
        ResearchFeatureEventRepository events,
        MealEntryRepository meals,
        HealthProfileRepository profiles,
        UserAccountRepository users
    ) {
    }
}

package in.aarogya.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.meals.repository.MealEntryRepository;
import in.aarogya.progress.domain.AchievementDefinition;
import in.aarogya.progress.domain.UserAchievement;
import in.aarogya.progress.repository.AchievementDefinitionRepository;
import in.aarogya.progress.repository.UserAchievementRepository;
import in.aarogya.progress.repository.UserWellnessGoalRepository;
import in.aarogya.progress.repository.WellnessGoalTemplateRepository;
import in.aarogya.progress.service.ProgressPolicy;
import in.aarogya.progress.service.ProgressService;
import in.aarogya.security.SecurityAuditService;

class ProgressServiceAchievementTests {

    @Test
    void evaluationAwardsMetMilestoneOnce() {
        var meals = mock(MealEntryRepository.class);
        var templates = mock(WellnessGoalTemplateRepository.class);
        var goals = mock(UserWellnessGoalRepository.class);
        var definitions = mock(AchievementDefinitionRepository.class);
        var earned = mock(UserAchievementRepository.class);
        var users = mock(UserAccountRepository.class);
        var audit = mock(SecurityAuditService.class);
        var user = mock(UserAccount.class);
        var definition = mock(AchievementDefinition.class);
        var userId = UUID.randomUUID();
        var today = LocalDate.of(2026, 10, 7);

        when(meals.findDistinctMealDatesByUserId(userId)).thenReturn(
            List.of(
                LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 6),
                LocalDate.of(2026, 10, 7)
            )
        );
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(definition.getAchievementCode()).thenReturn("THREE_DAY_RUN");
        when(definition.getCriteriaCode()).thenReturn("LONGEST_LOGGING_RUN");
        when(definition.getThresholdValue()).thenReturn(3);
        when(definition.getTitle()).thenReturn("Three-day rhythm");
        when(definition.getDescription()).thenReturn("Milestone");
        when(definitions.findByActiveTrueOrderByDisplayOrderAsc())
            .thenReturn(List.of(definition));

        var saved = mock(UserAchievement.class);
        when(saved.getDefinition()).thenReturn(definition);
        when(earned.findByUser_IdOrderByEarnedAtAsc(userId))
            .thenReturn(List.of(), List.of(saved));
        when(goals.findByUser_IdAndTemplate_GoalCode(
            userId,
            "MEAL_LOGGING_DAYS"
        )).thenReturn(Optional.empty());

        when(earned.save(org.mockito.ArgumentMatchers.any(UserAchievement.class)))
            .thenReturn(saved);

        var service = new ProgressService(
            meals, templates, goals, definitions, earned, users, audit,
            new ProgressPolicy()
        );

        var result = service.evaluate(userId, today);

        assertEquals(1, result.achievements().size());
        assertEquals(true, result.achievements().get(0).earned());

        var captor = ArgumentCaptor.forClass(UserAchievement.class);
        verify(earned).save(captor.capture());
        verify(audit).record(
            user,
            "ACHIEVEMENTS_EVALUATED",
            "SUCCESS",
            "progress",
            "newlyEarned=1"
        );
    }

    @Test
    void existingAchievementIsNeverAwardedAgain() {
        var meals = mock(MealEntryRepository.class);
        var templates = mock(WellnessGoalTemplateRepository.class);
        var goals = mock(UserWellnessGoalRepository.class);
        var definitions = mock(AchievementDefinitionRepository.class);
        var earned = mock(UserAchievementRepository.class);
        var users = mock(UserAccountRepository.class);
        var audit = mock(SecurityAuditService.class);
        var definition = mock(AchievementDefinition.class);
        var existing = mock(UserAchievement.class);
        var userId = UUID.randomUUID();

        when(meals.findDistinctMealDatesByUserId(userId)).thenReturn(
            List.of(LocalDate.of(2026, 10, 7))
        );
        when(definition.getAchievementCode()).thenReturn("FIRST_LOG");
        when(definition.getCriteriaCode()).thenReturn("TOTAL_LOGGING_DAYS");
        when(definition.getThresholdValue()).thenReturn(1);
        when(definition.getTitle()).thenReturn("First record");
        when(definition.getDescription()).thenReturn("Milestone");
        when(existing.getDefinition()).thenReturn(definition);
        when(existing.getEvidenceValue()).thenReturn(1);
        when(definitions.findByActiveTrueOrderByDisplayOrderAsc())
            .thenReturn(List.of(definition));
        when(earned.findByUser_IdOrderByEarnedAtAsc(userId))
            .thenReturn(List.of(existing));
        when(goals.findByUser_IdAndTemplate_GoalCode(
            userId,
            "MEAL_LOGGING_DAYS"
        )).thenReturn(Optional.empty());
        when(users.findById(userId)).thenReturn(Optional.of(mock(UserAccount.class)));

        var service = new ProgressService(
            meals, templates, goals, definitions, earned, users, audit,
            new ProgressPolicy()
        );

        service.evaluate(userId, LocalDate.of(2026, 10, 7));

        verify(earned, never())
            .save(org.mockito.ArgumentMatchers.any(UserAchievement.class));
    }
}

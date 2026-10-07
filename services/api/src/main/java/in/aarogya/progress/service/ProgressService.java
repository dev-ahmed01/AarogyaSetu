package in.aarogya.progress.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.meals.repository.MealEntryRepository;
import in.aarogya.progress.api.AchievementResponse;
import in.aarogya.progress.api.GoalProgressResponse;
import in.aarogya.progress.api.ProgressOverviewResponse;
import in.aarogya.progress.api.StreakResponse;
import in.aarogya.progress.domain.AchievementDefinition;
import in.aarogya.progress.domain.UserAchievement;
import in.aarogya.progress.domain.UserWellnessGoal;
import in.aarogya.progress.repository.AchievementDefinitionRepository;
import in.aarogya.progress.repository.UserAchievementRepository;
import in.aarogya.progress.repository.UserWellnessGoalRepository;
import in.aarogya.progress.repository.WellnessGoalTemplateRepository;
import in.aarogya.security.SecurityAuditService;

@Service
public class ProgressService {

    private static final String MEAL_LOGGING_GOAL = "MEAL_LOGGING_DAYS";

    private final MealEntryRepository mealRepository;
    private final WellnessGoalTemplateRepository templateRepository;
    private final UserWellnessGoalRepository goalRepository;
    private final AchievementDefinitionRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserAccountRepository userRepository;
    private final SecurityAuditService auditService;
    private final ProgressPolicy policy;

    public ProgressService(
        MealEntryRepository mealRepository,
        WellnessGoalTemplateRepository templateRepository,
        UserWellnessGoalRepository goalRepository,
        AchievementDefinitionRepository achievementRepository,
        UserAchievementRepository userAchievementRepository,
        UserAccountRepository userRepository,
        SecurityAuditService auditService,
        ProgressPolicy policy
    ) {
        this.mealRepository = mealRepository;
        this.templateRepository = templateRepository;
        this.goalRepository = goalRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.policy = policy;
    }

    @Transactional(readOnly = true)
    public ProgressOverviewResponse overview(UUID userId, LocalDate requestedDate) {
        var today = requestedDate == null ? LocalDate.now() : requestedDate;
        var dates = mealRepository.findDistinctMealDatesByUserId(userId);
        return buildOverview(userId, today, dates);
    }

    @Transactional
    public ProgressOverviewResponse evaluate(UUID userId, LocalDate requestedDate) {
        var today = requestedDate == null ? LocalDate.now() : requestedDate;
        var dates = mealRepository.findDistinctMealDatesByUserId(userId);
        var streaks = policy.streaks(dates, today);
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        var earnedByCode = userAchievementRepository
            .findByUser_IdOrderByEarnedAtAsc(userId)
            .stream()
            .collect(Collectors.toMap(
                item -> item.getDefinition().getAchievementCode(),
                Function.identity()
            ));

        var newlyEarned = 0;

        for (var definition : achievementRepository
            .findByActiveTrueOrderByDisplayOrderAsc()) {
            if (earnedByCode.containsKey(definition.getAchievementCode())) {
                continue;
            }

            var evidence = evidenceFor(definition, streaks);

            if (evidence >= definition.getThresholdValue()) {
                var earned = userAchievementRepository.save(
                    new UserAchievement(user, definition, evidence)
                );
                earnedByCode.put(definition.getAchievementCode(), earned);
                newlyEarned++;
            }
        }

        if (newlyEarned > 0) {
            auditService.record(
                user,
                "ACHIEVEMENTS_EVALUATED",
                "SUCCESS",
                "progress",
                "newlyEarned=" + newlyEarned
            );
        }

        return buildOverview(userId, today, dates);
    }

    @Transactional
    public ProgressOverviewResponse upsertMealLoggingGoal(
        UUID userId,
        int targetDaysPerWeek
    ) {
        var template = templateRepository.findById(MEAL_LOGGING_GOAL)
            .filter(item -> item.isActive())
            .orElseThrow(() -> new IllegalStateException(
                "Meal logging goal template is unavailable."
            ));

        if (targetDaysPerWeek < template.getMinTarget()
            || targetDaysPerWeek > template.getMaxTarget()) {
            throw new IllegalArgumentException(
                "Weekly target must be between "
                    + template.getMinTarget()
                    + " and "
                    + template.getMaxTarget()
                    + " days."
            );
        }

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        var goal = goalRepository
            .findByUser_IdAndTemplate_GoalCode(userId, MEAL_LOGGING_GOAL)
            .orElseGet(() -> new UserWellnessGoal(
                user,
                template,
                targetDaysPerWeek,
                LocalDate.now()
            ));

        goal.updateTarget(targetDaysPerWeek);
        goalRepository.save(goal);

        auditService.record(
            user,
            "WELLNESS_GOAL_UPDATED",
            "SUCCESS",
            MEAL_LOGGING_GOAL,
            "targetDaysPerWeek=" + targetDaysPerWeek
        );

        return evaluate(userId, LocalDate.now());
    }

    @Transactional
    public ProgressOverviewResponse pauseMealLoggingGoal(UUID userId) {
        var goal = requireGoal(userId);
        goal.pause();
        auditGoalState(userId, "WELLNESS_GOAL_PAUSED");
        return overview(userId, LocalDate.now());
    }

    @Transactional
    public ProgressOverviewResponse resumeMealLoggingGoal(UUID userId) {
        var goal = requireGoal(userId);
        goal.resume();
        auditGoalState(userId, "WELLNESS_GOAL_RESUMED");
        return overview(userId, LocalDate.now());
    }

    private UserWellnessGoal requireGoal(UUID userId) {
        return goalRepository
            .findByUser_IdAndTemplate_GoalCode(userId, MEAL_LOGGING_GOAL)
            .orElseThrow(() -> new IllegalArgumentException(
                "Create a meal-logging goal first."
            ));
    }

    private void auditGoalState(UUID userId, String eventType) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        auditService.record(
            user,
            eventType,
            "SUCCESS",
            MEAL_LOGGING_GOAL,
            null
        );
    }

    private ProgressOverviewResponse buildOverview(
        UUID userId,
        LocalDate today,
        List<LocalDate> dates
    ) {
        var week = policy.week(today);
        var streaks = policy.streaks(dates, today);
        var currentWeek = policy.loggingDaysInWindow(
            dates,
            week.start(),
            week.end()
        );
        var goal = goalRepository
            .findByUser_IdAndTemplate_GoalCode(userId, MEAL_LOGGING_GOAL)
            .map(item -> toGoal(item, currentWeek, week))
            .orElse(null);

        var earned = userAchievementRepository
            .findByUser_IdOrderByEarnedAtAsc(userId)
            .stream()
            .collect(Collectors.toMap(
                item -> item.getDefinition().getAchievementCode(),
                Function.identity()
            ));

        var achievements = achievementRepository
            .findByActiveTrueOrderByDisplayOrderAsc()
            .stream()
            .map(definition -> toAchievement(
                definition,
                earned.get(definition.getAchievementCode())
            ))
            .toList();

        return new ProgressOverviewResponse(
            week.start(),
            week.end(),
            goal,
            new StreakResponse(
                streaks.currentRunDays(),
                streaks.longestRunDays(),
                streaks.totalLoggingDays(),
                streaks.todayLogged(),
                streaks.lastLoggedDate(),
                streakMessage(streaks)
            ),
            achievements,
            "Consistency is useful evidence, not a score of how well you ate. Pausing a goal never removes earned achievements."
        );
    }

    private GoalProgressResponse toGoal(
        UserWellnessGoal goal,
        int currentWeek,
        ProgressPolicy.WeekWindow week
    ) {
        var template = goal.getTemplate();
        var target = goal.getTargetValue();
        var completed = Math.min(currentWeek, target);

        return new GoalProgressResponse(
            goal.getId(),
            template.getGoalCode(),
            template.getTitle(),
            template.getDescription(),
            goal.getStatus(),
            target,
            currentWeek,
            policy.progressPercent(currentWeek, target),
            template.getPeriodCode(),
            week.start(),
            week.end(),
            completed >= target
                ? "Weekly target reached. Extra logging is optional."
                : completed + " of " + target + " chosen days recorded."
        );
    }

    private AchievementResponse toAchievement(
        AchievementDefinition definition,
        UserAchievement earned
    ) {
        return new AchievementResponse(
            definition.getAchievementCode(),
            definition.getTitle(),
            definition.getDescription(),
            definition.getCriteriaCode(),
            definition.getThresholdValue(),
            earned != null,
            earned == null ? null : earned.getEarnedAt(),
            earned == null ? null : earned.getEvidenceValue()
        );
    }

    private int evidenceFor(
        AchievementDefinition definition,
        ProgressPolicy.StreakMetrics streaks
    ) {
        return switch (definition.getCriteriaCode()) {
            case "TOTAL_LOGGING_DAYS" -> streaks.totalLoggingDays();
            case "LONGEST_LOGGING_RUN" -> streaks.longestRunDays();
            default -> 0;
        };
    }

    private String streakMessage(ProgressPolicy.StreakMetrics streaks) {
        if (streaks.totalLoggingDays() == 0) {
            return "No run yet. One logged day is enough to begin.";
        }

        if (streaks.todayLogged()) {
            return streaks.currentRunDays()
                + "-day current run. There is no bonus for making it perfect.";
        }

        if (streaks.currentRunDays() > 0) {
            return "Your current run is still intact from yesterday. Today is not treated as missed while it is still in progress.";
        }

        return "No current run, and nothing was lost. Your longest run and earned milestones stay recorded.";
    }
}

package in.aarogya.recommendations.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.meals.api.DailyMealLogResponse;
import in.aarogya.meals.service.MealLogService;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.profile.service.ProfileService;
import in.aarogya.recommendations.api.EvidenceSourceResponse;
import in.aarogya.recommendations.api.RecommendationAssessmentResponse;
import in.aarogya.recommendations.api.RecommendationItemResponse;
import in.aarogya.recommendations.domain.RecommendationRule;
import in.aarogya.recommendations.repository.RecommendationRuleRepository;

@Service
public class RecommendationEngineService {

    private static final int WINDOW_DAYS = 7;
    private static final int MINIMUM_TREND_DAYS = 2;

    private final ProfileService profileService;
    private final MealLogService mealLogService;
    private final RecommendationRuleRepository ruleRepository;
    private final RecommendationPolicy policy;

    public RecommendationEngineService(
        ProfileService profileService,
        MealLogService mealLogService,
        RecommendationRuleRepository ruleRepository,
        RecommendationPolicy policy
    ) {
        this.profileService = profileService;
        this.mealLogService = mealLogService;
        this.ruleRepository = ruleRepository;
        this.policy = policy;
    }

    @Transactional(readOnly = true)
    public RecommendationAssessmentResponse assess(
        UUID userId,
        LocalDate requestedDate
    ) {
        var assessmentDate = requestedDate == null
            ? LocalDate.now()
            : requestedDate;

        var profile = profileService.getProfile(userId);

        if (!profile.onboardingComplete()) {
            return blocked(
                "PROFILE_INCOMPLETE",
                assessmentDate,
                "Complete your profile before Aarogya evaluates personalized guidance."
            );
        }

        if (!profile.personalizationConsentGranted()) {
            return blocked(
                "PERSONALIZATION_PAUSED",
                assessmentDate,
                "Personalization is paused. Re-enable consent before profile data is used for guidance."
            );
        }

        var analysisTo = assessmentDate.minusDays(1);
        var analysisFrom = analysisTo.minusDays(WINDOW_DAYS - 1L);
        var history = mealLogService.history(userId, analysisFrom, analysisTo);
        var eligibleDays = history.days().stream()
            .filter(policy::isTrendDayEligible)
            .toList();
        var today = mealLogService.day(userId, assessmentDate);

        var rules = latestRules();
        var recommendations = new ArrayList<RecommendationItemResponse>();
        var notices = baseNotices(profile);

        evaluateAllergyConflicts(
            profile,
            today,
            rules.get("ALLERGEN_CONFLICT"),
            recommendations
        );

        evaluateDietaryPattern(
            profile,
            today,
            rules.get("DIETARY_PATTERN_CONFLICT"),
            recommendations,
            notices
        );

        if (eligibleDays.size() >= MINIMUM_TREND_DAYS) {
            evaluateFibreTrend(
                profile,
                eligibleDays,
                rules.get("FIBRE_TREND_LOW"),
                recommendations
            );
            evaluateProteinTrend(
                profile,
                eligibleDays,
                rules.get("PROTEIN_TREND_LOW"),
                recommendations,
                notices
            );
        } else {
            notices.add(
                "Trend guidance needs at least two completed days with three food entries across at least two meal slots. This is an Aarogya data-quality heuristic, not a nutrition guideline."
            );
        }

        recommendations.sort(
            java.util.Comparator
                .comparingInt(RecommendationItemResponse::priority)
                .thenComparing(RecommendationItemResponse::ruleCode)
        );

        var status = statusFor(
            eligibleDays.size(),
            recommendations,
            notices
        );

        return new RecommendationAssessmentResponse(
            status,
            assessmentDate,
            analysisFrom,
            analysisTo,
            WINDOW_DAYS,
            eligibleDays.size(),
            MINIMUM_TREND_DAYS,
            List.copyOf(recommendations),
            List.copyOf(notices)
        );
    }

    private void evaluateAllergyConflicts(
        ProfileResponse profile,
        DailyMealLogResponse today,
        RecommendationRule rule,
        List<RecommendationItemResponse> recommendations
    ) {
        if (rule == null || profile.allergies().isEmpty()) {
            return;
        }

        for (var entry : today.entries()) {
            var conflicts = policy.allergenConflicts(profile.allergies(), entry);

            if (conflicts.isEmpty()) {
                continue;
            }

            recommendations.add(new RecommendationItemResponse(
                rule.getRuleCode(),
                rule.getRuleVersion(),
                0,
                rule.getTitle(),
                rule.getReasonCode(),
                rule.getSafetyClass(),
                entry.foodName() + " is flagged with " + String.join(", ", conflicts)
                    + ", which conflicts with the allergy information in your profile.",
                "This is a catalog/profile consistency warning. Aarogya cannot determine whether you consumed an allergen or predict reaction risk.",
                "Review the logged food and its ingredient or package label. Use your existing allergy care plan or seek medical help if needed.",
                null,
                null,
                null,
                EvidenceSourceResponse.from(rule.getEvidenceSource())
            ));
        }
    }

    private void evaluateDietaryPattern(
        ProfileResponse profile,
        DailyMealLogResponse today,
        RecommendationRule rule,
        List<RecommendationItemResponse> recommendations,
        List<String> notices
    ) {
        if (rule == null || profile.dietaryPattern() == null) {
            return;
        }

        if ("JAIN".equals(profile.dietaryPattern())) {
            notices.add(
                "Jain compatibility is not automatically evaluated yet because the current catalog does not encode ingredient-level Jain constraints completely."
            );
            return;
        }

        for (var entry : today.entries()) {
            if (policy.isDietCompatible(
                profile.dietaryPattern(),
                entry.dietaryClassification()
            )) {
                continue;
            }

            recommendations.add(new RecommendationItemResponse(
                rule.getRuleCode(),
                rule.getRuleVersion(),
                1,
                rule.getTitle(),
                rule.getReasonCode(),
                rule.getSafetyClass(),
                entry.foodName() + " is classified as "
                    + humanize(entry.dietaryClassification())
                    + " while your profile is "
                    + humanize(profile.dietaryPattern()) + ".",
                "This may simply mean the profile or catalog entry needs correction. It is not a health-risk judgment.",
                "Review the meal entry or update your dietary pattern if it no longer reflects your preferences.",
                null,
                null,
                null,
                EvidenceSourceResponse.from(rule.getEvidenceSource())
            ));
        }
    }

    private void evaluateFibreTrend(
        ProfileResponse profile,
        List<DailyMealLogResponse> eligibleDays,
        RecommendationRule rule,
        List<RecommendationItemResponse> recommendations
    ) {
        if (rule == null || !ageEligible(profile, rule)) {
            return;
        }

        var observed = policy.averageNutrient(
            eligibleDays,
            rule.getNutrientCode()
        );
        var reference = rule.getReferenceValue();

        if (!policy.belowTrigger(observed, reference, rule.getTriggerRatio())) {
            return;
        }

        recommendations.add(new RecommendationItemResponse(
            rule.getRuleCode(),
            rule.getRuleVersion(),
            priorityFor(profile, rule.getRuleCode()),
            rule.getTitle(),
            rule.getReasonCode(),
            rule.getSafetyClass(),
            "Across " + eligibleDays.size()
                + " sufficiently logged days, average fibre was "
                + format(observed) + " g/day.",
            "WHO guidance for people older than 10 uses at least "
                + format(reference)
                + " g/day of naturally occurring dietary fibre as a general reference.",
            "Consider making a future meal include a fibre-rich food category such as pulses, vegetables, fruit or whole grains.",
            observed,
            reference,
            "g/day",
            EvidenceSourceResponse.from(rule.getEvidenceSource())
        ));
    }

    private void evaluateProteinTrend(
        ProfileResponse profile,
        List<DailyMealLogResponse> eligibleDays,
        RecommendationRule rule,
        List<RecommendationItemResponse> recommendations,
        List<String> notices
    ) {
        if (rule == null || !ageEligible(profile, rule)) {
            return;
        }

        if (policy.suppressGenericProteinRule(profile)) {
            if (profile.healthContexts().contains("KIDNEY_CONDITION")) {
                notices.add(
                    "Generic protein targeting is suppressed because kidney-related needs should not be inferred by this wellness prototype."
                );
            } else if (profile.healthContexts().contains("PREGNANCY_OR_BREASTFEEDING")) {
                notices.add(
                    "Generic protein targeting is suppressed because pregnancy or breastfeeding changes nutrition requirements."
                );
            } else if (profile.weightKg() == null) {
                notices.add(
                    "Protein trend targeting is unavailable because no weight is stored in the profile."
                );
            }
            return;
        }

        var observed = policy.averageNutrient(
            eligibleDays,
            rule.getNutrientCode()
        );
        var reference = policy.adultProteinReference(
            profile.weightKg(),
            rule.getReferenceValue()
        );

        if (!policy.belowTrigger(observed, reference, rule.getTriggerRatio())) {
            return;
        }

        recommendations.add(new RecommendationItemResponse(
            rule.getRuleCode(),
            rule.getRuleVersion(),
            priorityFor(profile, rule.getRuleCode()),
            rule.getTitle(),
            rule.getReasonCode(),
            rule.getSafetyClass(),
            "Across " + eligibleDays.size()
                + " sufficiently logged days, average protein was "
                + format(observed) + " g/day.",
            "The WHO/FAO/UNU adult safe-level reference used by this prototype is "
                + format(rule.getReferenceValue())
                + " g/kg/day. With the weight stored in your profile, that corresponds to about "
                + format(reference) + " g/day.",
            "Consider reviewing whether your regular meals include a protein source. Aarogya does not prescribe a therapeutic protein target.",
            observed,
            reference,
            "g/day",
            EvidenceSourceResponse.from(rule.getEvidenceSource())
        ));
    }

    private Map<String, RecommendationRule> latestRules() {
        var latest = new LinkedHashMap<String, RecommendationRule>();

        for (var rule : ruleRepository.findByActiveTrueOrderByRuleCodeAscRuleVersionDesc()) {
            latest.putIfAbsent(rule.getRuleCode(), rule);
        }

        return latest;
    }

    private List<String> baseNotices(ProfileResponse profile) {
        var notices = new ArrayList<String>();
        notices.add(
            "Aarogya guidance is a wellness/research interpretation of logged data, not a diagnosis or treatment plan."
        );

        if (profile.goals().contains("WEIGHT_MANAGEMENT")) {
            notices.add(
                "Weight-management goals do not trigger calorie-deficit prescriptions in this prototype."
            );
        }

        if (!profile.healthContexts().isEmpty()) {
            notices.add(
                "Self-reported health context is used only for safety limits in Phase 7; Aarogya does not diagnose or create disease-specific diets."
            );
        }

        return notices;
    }

    private RecommendationAssessmentResponse blocked(
        String status,
        LocalDate assessmentDate,
        String notice
    ) {
        return new RecommendationAssessmentResponse(
            status,
            assessmentDate,
            assessmentDate.minusDays(WINDOW_DAYS),
            assessmentDate.minusDays(1),
            WINDOW_DAYS,
            0,
            MINIMUM_TREND_DAYS,
            List.of(),
            List.of(notice)
        );
    }

    private String statusFor(
        int observedDays,
        List<RecommendationItemResponse> recommendations,
        List<String> notices
    ) {
        var hasSafety = recommendations.stream()
            .anyMatch(item -> "SAFETY_ATTENTION".equals(item.safetyClass()));

        if (hasSafety) {
            return observedDays >= MINIMUM_TREND_DAYS
                ? "READY_WITH_SAFETY_ATTENTION"
                : "LIMITED_DATA_WITH_SAFETY_ATTENTION";
        }

        if (observedDays < MINIMUM_TREND_DAYS) {
            return "NEED_MORE_DATA";
        }

        return notices.size() > 1 ? "READY_WITH_LIMITS" : "READY";
    }

    private boolean ageEligible(
        ProfileResponse profile,
        RecommendationRule rule
    ) {
        if (profile.ageYears() == null) {
            return false;
        }

        if (rule.getMinimumAgeYears() != null
            && profile.ageYears() < rule.getMinimumAgeYears()) {
            return false;
        }

        return rule.getMaximumAgeYears() == null
            || profile.ageYears() <= rule.getMaximumAgeYears();
    }

    private int priorityFor(ProfileResponse profile, String ruleCode) {
        if ("PROTEIN_TREND_LOW".equals(ruleCode)
            && profile.goals().contains("MUSCLE_GAIN")) {
            return 1;
        }

        if ("FIBRE_TREND_LOW".equals(ruleCode)
            && (profile.goals().contains("HEART_HEALTH")
                || profile.goals().contains("BALANCED_NUTRITION"))) {
            return 1;
        }

        return 2;
    }

    private String humanize(String value) {
        if (value == null) {
            return "Unknown";
        }

        var lower = value.toLowerCase(java.util.Locale.ROOT)
            .replace('_', ' ');

        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String format(BigDecimal value) {
        return value
            .setScale(1, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString();
    }
}

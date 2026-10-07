package in.aarogya.plans.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.repository.FoodRepository;
import in.aarogya.plans.api.DietPlanResponse;
import in.aarogya.plans.api.GeneratePlanRequest;
import in.aarogya.plans.api.PlanDayResponse;
import in.aarogya.plans.api.SmartFoodSuggestionResponse;
import in.aarogya.plans.domain.DietPlan;
import in.aarogya.plans.domain.DietPlanItem;
import in.aarogya.plans.repository.DietPlanRepository;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.profile.service.ProfileService;
import in.aarogya.recommendations.service.RecommendationEngineService;
import in.aarogya.regional.service.RegionalIntelligenceService;
import in.aarogya.regional.service.RegionalIntelligenceService.RegionalFit;

@Service
public class DietPlanService {

    private static final List<String> MEAL_TYPES = List.of(
        "BREAKFAST", "LUNCH", "DINNER", "SNACK"
    );

    private final DietPlanRepository planRepository;
    private final FoodRepository foodRepository;
    private final UserAccountRepository userRepository;
    private final ProfileService profileService;
    private final RecommendationEngineService recommendationEngine;
    private final PlanGenerationPolicy policy;
    private final RegionalIntelligenceService regionalService;

    public DietPlanService(
        DietPlanRepository planRepository,
        FoodRepository foodRepository,
        UserAccountRepository userRepository,
        ProfileService profileService,
        RecommendationEngineService recommendationEngine,
        PlanGenerationPolicy policy,
        RegionalIntelligenceService regionalService
    ) {
        this.planRepository = planRepository;
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.recommendationEngine = recommendationEngine;
        this.policy = policy;
        this.regionalService = regionalService;
    }

    @Transactional(readOnly = true)
    public PlanDayResponse current(UUID userId, LocalDate requestedDate) {
        var date = requestedDate == null ? LocalDate.now() : requestedDate;
        var profile = profileService.getProfile(userId);

        var plan = planRepository
            .findTopByUser_IdAndPlanDateAndStatusOrderByCreatedAtDesc(
                userId, date, "DRAFT"
            )
            .orElse(null);

        if (plan == null) {
            return new PlanDayResponse(date, false, null);
        }

        return new PlanDayResponse(
            date,
            true,
            DietPlanResponse.from(
                plan,
                notices(
                    profile,
                    plan.getGenerationMode(),
                    plan.getRegionalContextLabel()
                )
            )
        );
    }

    @Transactional(readOnly = true)
    public List<SmartFoodSuggestionResponse> suggestions(
        UUID userId,
        LocalDate requestedDate,
        int limit
    ) {
        var date = requestedDate == null ? LocalDate.now() : requestedDate;
        var profile = requireReadyProfile(userId);
        var assessment = recommendationEngine.assess(userId, date);
        var focus = policy.focusFrom(assessment);
        var candidates = eligibleFoods(profile);
        var fits = regionalService.fitsFor(
            candidates,
            profile.stateOrRegion()
        );

        return rankedFoods(
            candidates,
            focus,
            null,
            Set.of(),
            fits
        ).stream()
            .limit(Math.min(Math.max(limit, 1), 12))
            .map(food -> toSuggestion(food, focus, fits.get(food.getId())))
            .toList();
    }

    @Transactional
    public DietPlanResponse generate(
        UUID userId,
        GeneratePlanRequest request
    ) {
        var profile = requireReadyProfile(userId);
        var date = request.planDate();
        validatePlanDate(date);

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        var assessment = recommendationEngine.assess(userId, date);
        var focus = policy.focusFrom(assessment);
        var context = regionalService.context(userId);
        var candidates = eligibleFoods(profile);
        var fits = regionalService.fitsFor(
            candidates,
            profile.stateOrRegion()
        );

        if (candidates.isEmpty()) {
            throw new PlanGenerationUnavailableException(
                "No source-referenced foods remain after your recorded diet and allergy filters."
            );
        }

        planRepository.findByUser_IdAndPlanDateAndStatus(
            userId, date, "DRAFT"
        ).forEach(DietPlan::archive);

        var contextCode = context.stateCode() != null
            ? context.stateCode()
            : context.macroRegionCode();
        var contextLabel = context.stateLabel() != null
            ? context.stateLabel()
            : context.macroRegionLabel();

        var plan = new DietPlan(
            user,
            date,
            focus.mode(),
            assessment.status(),
            focus.sourceRuleCode(),
            focus.sourceRuleVersion(),
            context.supported() ? contextCode : null,
            context.supported() ? contextLabel : null
        );

        var usedSlugs = new HashSet<String>();
        var usedCategories = new HashSet<String>();
        var displayOrder = 0;

        for (var mealType : MEAL_TYPES) {
            var selected = chooseForMeal(
                candidates,
                focus,
                mealType,
                usedSlugs,
                usedCategories,
                fits
            );

            if (selected == null) {
                continue;
            }

            var portion = policy.defaultPortion(selected);
            var grams = portion == null
                ? new BigDecimal("100.00")
                : portion.getGrams().setScale(2, RoundingMode.HALF_UP);
            var fit = fits.get(selected.getId());
            var explanation = explanation(
                selected,
                mealType,
                focus,
                grams,
                fit
            );

            plan.addItem(new DietPlanItem(
                plan,
                mealType,
                displayOrder++,
                selected,
                portion,
                grams,
                focus.nutrientCode(),
                focus.reasonCode(),
                explanation,
                fit == null ? null : fit.score(),
                fit == null ? null : regionalService.fitLabel(fit),
                fit == null ? null : fit.affinity().getRationale()
            ));

            usedSlugs.add(selected.getSlug());
            usedCategories.add(selected.getCategoryCode());
        }

        if (plan.getItems().isEmpty()) {
            throw new PlanGenerationUnavailableException(
                "Aarogya could not build a safe draft from the currently eligible catalog."
            );
        }

        var saved = planRepository.save(plan);

        return DietPlanResponse.from(
            saved,
            notices(profile, focus.mode(), contextLabel)
        );
    }

    @Transactional
    public void archive(UUID userId, UUID planId) {
        var plan = planRepository.findByIdAndUser_Id(planId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Plan was not found."));
        plan.archive();
    }

    private List<Food> eligibleFoods(ProfileResponse profile) {
        return foodRepository
            .findByActiveTrueAndNutrientStatusOrderByCanonicalNameAsc(
                "SOURCE_REFERENCED"
            )
            .stream()
            .filter(food -> policy.isEligibleFood(profile, food))
            .toList();
    }

    private ProfileResponse requireReadyProfile(UUID userId) {
        var profile = profileService.getProfile(userId);

        if (!profile.onboardingComplete()) {
            throw new PlanGenerationUnavailableException(
                "Complete your profile before generating food suggestions."
            );
        }

        if (!profile.personalizationConsentGranted()) {
            throw new PlanGenerationUnavailableException(
                "Personalization is paused. Re-enable consent before generating suggestions."
            );
        }

        if ("JAIN".equals(profile.dietaryPattern())) {
            throw new PlanGenerationUnavailableException(
                "Automatic Jain meal planning is not enabled yet because the current food ontology does not encode ingredient-level Jain constraints completely."
            );
        }

        return profile;
    }

    private void validatePlanDate(LocalDate date) {
        var today = LocalDate.now();

        if (date.isBefore(today.minusDays(1))
            || date.isAfter(today.plusDays(14))) {
            throw new IllegalArgumentException(
                "Draft plans can be generated from yesterday through 14 days ahead."
            );
        }
    }

    private Food chooseForMeal(
        List<Food> candidates,
        PlanGenerationPolicy.Focus focus,
        String mealType,
        Set<String> usedSlugs,
        Set<String> usedCategories,
        Map<UUID, RegionalFit> fits
    ) {
        var ranked = rankedFoods(
            candidates,
            focus,
            mealType,
            usedCategories,
            fits
        );

        return ranked.stream()
            .filter(food -> !usedSlugs.contains(food.getSlug()))
            .findFirst()
            .orElseGet(() -> ranked.stream().findFirst().orElse(null));
    }

    private List<Food> rankedFoods(
        List<Food> foods,
        PlanGenerationPolicy.Focus focus,
        String mealType,
        Set<String> usedCategories,
        Map<UUID, RegionalFit> fits
    ) {
        return foods.stream()
            .sorted(
                Comparator
                    .comparingDouble(
                        (Food food) -> policy.score(
                            food,
                            focus,
                            mealType,
                            usedCategories,
                            regionalService.planningBonus(
                                fits.get(food.getId())
                            )
                        )
                    )
                    .reversed()
                    .thenComparing(Food::getCanonicalName)
            )
            .toList();
    }

    private SmartFoodSuggestionResponse toSuggestion(
        Food food,
        PlanGenerationPolicy.Focus focus,
        RegionalFit fit
    ) {
        var portion = policy.defaultPortion(food);
        var grams = portion == null
            ? new BigDecimal("100.00")
            : portion.getGrams().setScale(2, RoundingMode.HALF_UP);
        var focusAmount = policy.nutrientForGrams(
            food,
            focus.nutrientCode(),
            grams
        );

        return new SmartFoodSuggestionResponse(
            food.getSlug(),
            food.getCanonicalName(),
            food.getCategoryCode(),
            food.getDietaryClassification(),
            food.getPrimaryRegion(),
            portion == null ? "100 g" : portion.getLabel(),
            grams,
            focus.nutrientCode(),
            focusAmount,
            policy.nutrientUnit(food, focus.nutrientCode()),
            focus.reasonCode(),
            explanation(food, null, focus, grams, fit),
            food.getSource() == null ? null : food.getSource().getSourceCode(),
            food.getSourceFoodRef(),
            fit == null ? null : fit.score(),
            fit == null ? null : regionalService.fitLabel(fit),
            fit == null ? null : fit.affinity().getRationale()
        );
    }

    private String explanation(
        Food food,
        String mealType,
        PlanGenerationPolicy.Focus focus,
        BigDecimal grams,
        RegionalFit fit
    ) {
        var mealText = mealType == null
            ? "your food options"
            : mealType.toLowerCase(java.util.Locale.ROOT);
        var regionText = fit != null
            && !"ALL_INDIA".equals(fit.affinity().getRelationship())
                ? " A small regional-familiarity bonus also affected ordering after safety and nutrition checks."
                : "";

        if (focus.nutrientCode() == null) {
            return food.getCanonicalName()
                + " was selected for " + mealText
                + " as a source-referenced catalog option compatible with your recorded diet and allergy filters. The draft uses variety heuristics, not a prescribed calorie target."
                + regionText;
        }

        var amount = policy.nutrientForGrams(
            food,
            focus.nutrientCode(),
            grams
        );
        var unit = policy.nutrientUnit(food, focus.nutrientCode());
        var nutrientLabel = "FIBRE_G".equals(focus.nutrientCode())
            ? "fibre"
            : "protein";

        return food.getCanonicalName()
            + " was ranked as a source-referenced " + nutrientLabel
            + " option compatible with your recorded diet and allergy filters. This catalog portion contributes about "
            + amount.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            + " " + unit
            + " of " + nutrientLabel
            + "; it is an example option, not a prescribed target."
            + regionText;
    }

    private List<String> notices(
        ProfileResponse profile,
        String generationMode,
        String regionalContextLabel
    ) {
        var notices = new ArrayList<String>();
        notices.add(
            "This is a draft meal sketch for planning convenience, not a prescription or clinical diet."
        );
        notices.add(
            "Foods are filtered against recorded dietary pattern and catalog allergen flags before ranking."
        );

        if (regionalContextLabel != null) {
            notices.add(
                "Regional familiarity for "
                    + regionalContextLabel
                    + " can add only a small ranking bonus after safety, source and nutrition checks. It is cultural relevance metadata, not nutrition guidance."
            );
        }

        if ("BALANCED_FOUNDATION".equals(generationMode)) {
            notices.add(
                "No eligible nutrient-gap rule is driving this draft, so Aarogya uses source-referenced catalog variety rather than inventing a target."
            );
        }

        if (!profile.healthContexts().isEmpty()) {
            notices.add(
                "Self-reported health context does not generate disease-specific meal plans in this prototype."
            );
        }

        return List.copyOf(notices);
    }
}

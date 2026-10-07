package in.aarogya.regional.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.nutrition.api.FoodSummaryResponse;
import in.aarogya.nutrition.domain.Food;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.profile.service.ProfileService;
import in.aarogya.recommendations.service.RecommendationPolicy;
import in.aarogya.regional.api.LocalizedAliasResponse;
import in.aarogya.regional.api.RegionalAlternativeResponse;
import in.aarogya.regional.api.RegionalContextResponse;
import in.aarogya.regional.api.RegionalFoodResponse;
import in.aarogya.regional.domain.FoodRegionalAffinity;
import in.aarogya.regional.repository.FoodLocalizedAliasRepository;
import in.aarogya.regional.repository.FoodRegionalAffinityRepository;
import in.aarogya.regional.repository.RegionalFoodAlternativeRepository;

@Service
public class RegionalIntelligenceService {

    private final RegionResolver resolver;
    private final FoodRegionalAffinityRepository affinityRepository;
    private final FoodLocalizedAliasRepository aliasRepository;
    private final RegionalFoodAlternativeRepository alternativeRepository;
    private final ProfileService profileService;
    private final RecommendationPolicy recommendationPolicy;

    public RegionalIntelligenceService(
        RegionResolver resolver,
        FoodRegionalAffinityRepository affinityRepository,
        FoodLocalizedAliasRepository aliasRepository,
        RegionalFoodAlternativeRepository alternativeRepository,
        ProfileService profileService,
        RecommendationPolicy recommendationPolicy
    ) {
        this.resolver = resolver;
        this.affinityRepository = affinityRepository;
        this.aliasRepository = aliasRepository;
        this.alternativeRepository = alternativeRepository;
        this.profileService = profileService;
        this.recommendationPolicy = recommendationPolicy;
    }

    @Transactional(readOnly = true)
    public RegionalContextResponse context(UUID userId) {
        var profile = profileService.getProfile(userId);

        if (!profile.personalizationConsentGranted()) {
            return new RegionalContextResponse(
                null,
                null,
                null,
                null,
                null,
                false,
                List.of("ALL_INDIA"),
                "PERSONALIZATION_PAUSED",
                "Regional personalization is paused because profile-personalization consent is not granted."
            );
        }

        return resolver.resolve(profile.stateOrRegion());
    }

    @Transactional(readOnly = true)
    public List<RegionalFoodResponse> foods(UUID userId, int limit) {
        var profile = requirePersonalization(userId);
        var context = resolver.resolve(profile.stateOrRegion());
        var best = bestAffinities(
            affinityRepository.findByRegionCodeInOrderByAffinityScoreDesc(
                context.matchedRegionCodes()
            ),
            context
        );

        return best.values().stream()
            .filter(value -> isCompatible(profile, value.affinity().getFood()))
            .sorted(
                Comparator
                    .comparingInt((RegionalFit value) -> value.score())
                    .reversed()
                    .thenComparing(value ->
                        value.affinity().getFood().getCanonicalName()
                    )
            )
            .limit(Math.min(Math.max(limit, 1), 24))
            .map(this::toFoodResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RegionalAlternativeResponse> alternatives(
        UUID userId,
        String foodSlug
    ) {
        var profile = requirePersonalization(userId);
        var context = resolver.resolve(profile.stateOrRegion());

        return alternativeRepository
            .findBySourceFood_SlugAndRegionCodeInAndActiveTrueOrderByPriorityAsc(
                foodSlug,
                context.matchedRegionCodes()
            )
            .stream()
            .filter(item -> isCompatible(profile, item.getAlternativeFood()))
            .map(item -> new RegionalAlternativeResponse(
                item.getSourceFood().getSlug(),
                FoodSummaryResponse.from(item.getAlternativeFood()),
                item.getRegionCode(),
                item.getPriority(),
                item.getRationale(),
                isPlanningEligible(item.getAlternativeFood())
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public Map<UUID, RegionalFit> fitsFor(
        Collection<Food> foods,
        String profileRegion
    ) {
        if (foods.isEmpty()) {
            return Map.of();
        }

        var context = resolver.resolve(profileRegion);
        var ids = foods.stream().map(Food::getId).toList();

        return bestAffinities(
            affinityRepository.findByFood_IdInAndRegionCodeIn(
                ids,
                context.matchedRegionCodes()
            ),
            context
        );
    }

    public double planningBonus(RegionalFit fit) {
        if (fit == null) {
            return 0.0;
        }

        if ("ALL_INDIA".equals(fit.affinity().getRelationship())) {
            return 2.0;
        }

        return Math.min(12.0, fit.score() * 0.12);
    }

    public String fitLabel(RegionalFit fit) {
        if (fit == null) return "No regional signal";
        if ("STATE_FAMILIAR".equals(fit.affinity().getRelationship())) {
            return "State familiar";
        }
        if ("REGIONAL_FAMILIAR".equals(fit.affinity().getRelationship())) {
            return "Region familiar";
        }
        return "Broadly familiar";
    }

    private Map<UUID, RegionalFit> bestAffinities(
        List<FoodRegionalAffinity> affinities,
        RegionalContextResponse context
    ) {
        var best = new LinkedHashMap<UUID, RegionalFit>();

        for (var affinity : affinities) {
            var fit = new RegionalFit(
                affinity,
                effectiveScore(affinity, context)
            );
            best.merge(
                affinity.getFood().getId(),
                fit,
                (left, right) -> right.score() > left.score() ? right : left
            );
        }

        return Map.copyOf(best);
    }

    private int effectiveScore(
        FoodRegionalAffinity affinity,
        RegionalContextResponse context
    ) {
        var specificity = 0;

        if (context.stateCode() != null
            && context.stateCode().equals(affinity.getRegionCode())) {
            specificity = 8;
        } else if (context.macroRegionCode() != null
            && context.macroRegionCode().equals(affinity.getRegionCode())) {
            specificity = 4;
        }

        return Math.min(100, affinity.getAffinityScore() + specificity);
    }

    private RegionalFoodResponse toFoodResponse(RegionalFit fit) {
        var food = fit.affinity().getFood();
        var aliases = aliasRepository
            .findByFood_IdOrderByLocaleCodeAscAliasAsc(food.getId())
            .stream()
            .map(LocalizedAliasResponse::from)
            .toList();

        return new RegionalFoodResponse(
            FoodSummaryResponse.from(food),
            fit.score(),
            fitLabel(fit),
            fit.affinity().getRegionCode(),
            fit.affinity().getRelationship(),
            fit.affinity().getRationale(),
            fit.affinity().getSourceCode(),
            aliases,
            isPlanningEligible(food)
        );
    }

    private boolean isCompatible(ProfileResponse profile, Food food) {
        if (!food.isActive()) {
            return false;
        }

        if ("JAIN".equals(profile.dietaryPattern())) {
            return false;
        }

        if (!"SOURCE_REFERENCED".equals(food.getNutrientStatus())
            && !profile.allergies().isEmpty()) {
            return false;
        }

        if (!recommendationPolicy.isDietCompatible(
            profile.dietaryPattern(),
            food.getDietaryClassification()
        )) {
            return false;
        }

        return food.getAllergens().stream()
            .noneMatch(profile.allergies()::contains);
    }

    private boolean isPlanningEligible(Food food) {
        return "SOURCE_REFERENCED".equals(food.getNutrientStatus())
            && !food.getNutrients().isEmpty();
    }

    private ProfileResponse requirePersonalization(UUID userId) {
        var profile = profileService.getProfile(userId);

        if (!profile.onboardingComplete()) {
            throw new IllegalArgumentException(
                "Complete your profile before using regional personalization."
            );
        }

        if (!profile.personalizationConsentGranted()) {
            throw new IllegalArgumentException(
                "Regional personalization is paused because profile-personalization consent is not granted."
            );
        }

        return profile;
    }

    public record RegionalFit(
        FoodRegionalAffinity affinity,
        int score
    ) {
    }
}

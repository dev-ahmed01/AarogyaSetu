package in.aarogya.plans.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodNutrient;
import in.aarogya.nutrition.domain.FoodPortion;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.recommendations.api.RecommendationAssessmentResponse;
import in.aarogya.recommendations.api.RecommendationItemResponse;
import in.aarogya.recommendations.service.RecommendationPolicy;

@Component
public class PlanGenerationPolicy {

    private static final String SOURCE_REFERENCED = "SOURCE_REFERENCED";

    private final RecommendationPolicy recommendationPolicy;

    public PlanGenerationPolicy(RecommendationPolicy recommendationPolicy) {
        this.recommendationPolicy = recommendationPolicy;
    }

    public boolean isEligibleFood(ProfileResponse profile, Food food) {
        if (!food.isActive()
            || !SOURCE_REFERENCED.equals(food.getNutrientStatus())
            || food.getNutrients().isEmpty()) {
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

    public Focus focusFrom(RecommendationAssessmentResponse assessment) {
        for (RecommendationItemResponse item : assessment.recommendations()) {
            if ("FIBRE_TREND_LOW".equals(item.ruleCode())) {
                return new Focus(
                    "FIBRE_FOCUS",
                    "FIBRE_G",
                    item.reasonCode(),
                    item.ruleCode(),
                    item.ruleVersion()
                );
            }

            if ("PROTEIN_TREND_LOW".equals(item.ruleCode())) {
                return new Focus(
                    "PROTEIN_FOCUS",
                    "PROTEIN_G",
                    item.reasonCode(),
                    item.ruleCode(),
                    item.ruleVersion()
                );
            }
        }

        return new Focus(
            "BALANCED_FOUNDATION",
            null,
            "BALANCED_CATALOG_VARIETY",
            null,
            null
        );
    }

    public double score(
        Food food,
        Focus focus,
        String mealType,
        Set<String> usedCategories
    ) {
        double score = mealAffinity(food.getCategoryCode(), mealType);

        if (!usedCategories.contains(food.getCategoryCode())) {
            score += 8.0;
        }

        if (focus.nutrientCode() != null) {
            score += nutrientValue(food, focus.nutrientCode()).doubleValue() * 6.0;
        } else {
            score += nutrientValue(food, "PROTEIN_G").doubleValue();
            score += nutrientValue(food, "FIBRE_G").doubleValue() * 2.0;
        }

        return score;
    }

    public FoodPortion defaultPortion(Food food) {
        return food.getPortions().stream()
            .filter(FoodPortion::isDefaultPortion)
            .findFirst()
            .orElseGet(() -> food.getPortions().stream()
                .sorted(java.util.Comparator.comparingInt(FoodPortion::getDisplayOrder))
                .findFirst()
                .orElse(null));
    }

    public BigDecimal nutrientValue(Food food, String nutrientCode) {
        if (nutrientCode == null) {
            return BigDecimal.ZERO;
        }

        return food.getNutrients().stream()
            .filter(nutrient -> nutrientCode.equals(nutrient.getNutrientCode()))
            .map(FoodNutrient::getAmountPer100g)
            .findFirst()
            .orElse(BigDecimal.ZERO);
    }

    public BigDecimal nutrientForGrams(
        Food food,
        String nutrientCode,
        BigDecimal grams
    ) {
        if (nutrientCode == null) {
            return null;
        }

        return food.getNutrients().stream()
            .filter(nutrient -> nutrientCode.equals(nutrient.getNutrientCode()))
            .findFirst()
            .map(nutrient -> nutrient.amountForGrams(grams))
            .orElse(BigDecimal.ZERO);
    }

    public String nutrientUnit(Food food, String nutrientCode) {
        if (nutrientCode == null) {
            return null;
        }

        return food.getNutrients().stream()
            .filter(nutrient -> nutrientCode.equals(nutrient.getNutrientCode()))
            .map(FoodNutrient::getUnit)
            .findFirst()
            .orElse(null);
    }

    private double mealAffinity(String category, String mealType) {
        if (mealType == null) {
            return 0;
        }

        var preferred = switch (mealType) {
            case "BREAKFAST" -> Set.of(
                "FRUIT", "DAIRY", "EGG", "CEREAL", "NUT_AND_SEED"
            );
            case "LUNCH" -> Set.of(
                "PULSE", "CEREAL", "VEGETABLE"
            );
            case "DINNER" -> Set.of(
                "PULSE", "VEGETABLE", "CEREAL", "EGG"
            );
            case "SNACK" -> Set.of(
                "FRUIT", "DAIRY", "NUT_AND_SEED"
            );
            default -> Set.<String>of();
        };

        return preferred.contains(category) ? 35.0 : 0.0;
    }

    public record Focus(
        String mode,
        String nutrientCode,
        String reasonCode,
        String sourceRuleCode,
        Integer sourceRuleVersion
    ) {
    }
}

package in.aarogya.recommendations.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import in.aarogya.meals.api.DailyMealLogResponse;
import in.aarogya.meals.api.MealEntryResponse;
import in.aarogya.profile.api.ProfileResponse;

@Component
public class RecommendationPolicy {

    public boolean isTrendDayEligible(DailyMealLogResponse day) {
        if (day.entryCount() < 3) {
            return false;
        }

        var mealTypes = day.entries().stream()
            .map(MealEntryResponse::mealType)
            .distinct()
            .count();

        return mealTypes >= 2;
    }

    public BigDecimal averageNutrient(
        List<DailyMealLogResponse> days,
        String nutrientCode
    ) {
        if (days.isEmpty()) {
            return BigDecimal.ZERO;
        }

        var sum = days.stream()
            .map(day -> day.totals().stream()
                .filter(total -> nutrientCode.equals(total.code()))
                .map(total -> total.amount())
                .findFirst()
                .orElse(BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(
            BigDecimal.valueOf(days.size()),
            2,
            RoundingMode.HALF_UP
        );
    }

    public BigDecimal adultProteinReference(
        BigDecimal weightKg,
        BigDecimal gramsPerKg
    ) {
        return weightKg
            .multiply(gramsPerKg)
            .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean belowTrigger(
        BigDecimal observed,
        BigDecimal reference,
        BigDecimal triggerRatio
    ) {
        if (observed == null || reference == null || triggerRatio == null) {
            return false;
        }

        return observed.compareTo(
            reference.multiply(triggerRatio)
        ) < 0;
    }

    public boolean suppressGenericProteinRule(ProfileResponse profile) {
        return profile.weightKg() == null
            || profile.ageYears() == null
            || profile.ageYears() < 18
            || profile.healthContexts().contains("KIDNEY_CONDITION")
            || profile.healthContexts().contains("PREGNANCY_OR_BREASTFEEDING");
    }

    public boolean isDietCompatible(
        String dietaryPattern,
        String foodClassification
    ) {
        if (dietaryPattern == null || foodClassification == null) {
            return true;
        }

        return switch (dietaryPattern) {
            case "VEGAN" -> "VEGAN".equals(foodClassification);
            case "VEGETARIAN" -> Set.of("VEGAN", "VEGETARIAN")
                .contains(foodClassification);
            case "EGGETARIAN" -> Set.of("VEGAN", "VEGETARIAN", "EGGETARIAN")
                .contains(foodClassification);
            case "PESCATARIAN" -> Set.of(
                "VEGAN", "VEGETARIAN", "EGGETARIAN", "PESCATARIAN"
            ).contains(foodClassification);
            default -> true;
        };
    }

    public Set<String> allergenConflicts(
        Set<String> recordedAllergies,
        MealEntryResponse entry
    ) {
        return entry.allergens().stream()
            .filter(recordedAllergies::contains)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}

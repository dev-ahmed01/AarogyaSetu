package in.aarogya.meals.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import in.aarogya.meals.domain.MealEntry;
import in.aarogya.meals.domain.MealEntryNutrient;

public record MealEntryResponse(
    UUID id,
    String foodSlug,
    String foodName,
    LocalDate mealDate,
    String mealType,
    BigDecimal quantityGrams,
    UUID portionId,
    String portionLabel,
    BigDecimal portionCount,
    String sourceCode,
    String sourceFoodRef,
    String nutrientStatus,
    String dietaryClassification,
    java.util.Set<String> allergens,
    List<MealNutrientResponse> nutrients,
    Instant createdAt,
    Instant updatedAt
) {

    public static MealEntryResponse from(MealEntry entry) {
        var nutrients = entry.getNutrientSnapshots().stream()
            .sorted(Comparator.comparing(MealEntryNutrient::getNutrientCode))
            .map(MealNutrientResponse::from)
            .toList();

        return new MealEntryResponse(
            entry.getId(),
            entry.getFood().getSlug(),
            entry.getFoodNameSnapshot(),
            entry.getMealDate(),
            entry.getMealType(),
            entry.getQuantityGrams(),
            entry.getPortion() == null ? null : entry.getPortion().getId(),
            entry.getPortionLabelSnapshot(),
            entry.getPortionCount(),
            entry.getSourceCodeSnapshot(),
            entry.getSourceFoodRefSnapshot(),
            entry.getNutrientStatusSnapshot(),
            entry.getDietaryClassificationSnapshot(),
            entry.getAllergenSnapshots(),
            nutrients,
            entry.getCreatedAt(),
            entry.getUpdatedAt()
        );
    }
}

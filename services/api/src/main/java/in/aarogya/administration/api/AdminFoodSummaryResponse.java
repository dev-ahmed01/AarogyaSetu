package in.aarogya.administration.api;

import java.util.UUID;

import in.aarogya.nutrition.domain.Food;

public record AdminFoodSummaryResponse(
    UUID id,
    String slug,
    String name,
    String primaryRegion,
    String nutrientStatus,
    String curationStatus,
    boolean active,
    String sourceCode,
    int nutrientCount,
    int portionCount,
    boolean publishReady
) {
    public static AdminFoodSummaryResponse from(
        Food food,
        int nutrientCount,
        int portionCount,
        boolean publishReady
    ) {
        return new AdminFoodSummaryResponse(
            food.getId(),
            food.getSlug(),
            food.getCanonicalName(),
            food.getPrimaryRegion(),
            food.getNutrientStatus(),
            food.getCurationStatus(),
            food.isActive(),
            food.getSource() == null
                ? null
                : food.getSource().getSourceCode(),
            nutrientCount,
            portionCount,
            publishReady
        );
    }
}

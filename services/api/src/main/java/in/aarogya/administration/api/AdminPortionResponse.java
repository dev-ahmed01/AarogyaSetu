package in.aarogya.administration.api;

import java.math.BigDecimal;
import java.util.UUID;

import in.aarogya.nutrition.domain.FoodPortion;

public record AdminPortionResponse(
    UUID id,
    String label,
    BigDecimal grams,
    boolean defaultPortion,
    int displayOrder
) {
    public static AdminPortionResponse from(FoodPortion portion) {
        return new AdminPortionResponse(
            portion.getId(),
            portion.getLabel(),
            portion.getGrams(),
            portion.isDefaultPortion(),
            portion.getDisplayOrder()
        );
    }
}

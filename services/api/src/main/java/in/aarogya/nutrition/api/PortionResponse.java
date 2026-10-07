package in.aarogya.nutrition.api;

import java.math.BigDecimal;

import in.aarogya.nutrition.domain.FoodPortion;

public record PortionResponse(
    String id,
    String label,
    BigDecimal grams,
    boolean defaultPortion
) {

    public static PortionResponse from(FoodPortion portion) {
        return new PortionResponse(
            portion.getId().toString(),
            portion.getLabel(),
            portion.getGrams(),
            portion.isDefaultPortion()
        );
    }
}

package in.aarogya.meals.api;

import java.math.BigDecimal;

public record NutrientTotalResponse(
    String code,
    BigDecimal amount,
    String unit
) {
}

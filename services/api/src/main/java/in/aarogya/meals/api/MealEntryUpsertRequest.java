package in.aarogya.meals.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MealEntryUpsertRequest(
    @NotBlank
    @Size(max = 140)
    String foodSlug,

    @NotNull
    LocalDate mealDate,

    @NotBlank
    @Size(max = 20)
    String mealType,

    UUID portionId,

    @DecimalMin("0.01")
    @DecimalMax("50.0")
    BigDecimal portionCount,

    @DecimalMin("1.0")
    @DecimalMax("5000.0")
    BigDecimal grams
) {
}

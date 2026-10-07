package in.aarogya.administration.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CuratedNutrientRequest(
    @NotBlank
    @Size(max = 60)
    String code,

    @NotNull
    @DecimalMin("0.0")
    BigDecimal amountPer100g,

    @NotBlank
    @Size(max = 20)
    String unit
) {
}

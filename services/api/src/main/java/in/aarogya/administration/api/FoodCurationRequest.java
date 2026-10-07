package in.aarogya.administration.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FoodCurationRequest(
    @NotNull
    UUID sourceId,

    @NotBlank
    @Size(max = 120)
    String sourceFoodRef,

    @NotNull
    @Size(min = 5, max = 30)
    List<@Valid CuratedNutrientRequest> nutrients,

    @NotBlank
    @Size(max = 100)
    String defaultPortionLabel,

    @NotNull
    @DecimalMin("1.0")
    @DecimalMax("5000.0")
    BigDecimal defaultPortionGrams,

    @Size(max = 1000)
    String note
) {
}

package in.aarogya.profile.api;

import java.math.BigDecimal;
import java.util.Set;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
    @Min(13) @Max(120)
    Integer ageYears,

    @Size(max = 30)
    String sexForNutrition,

    @DecimalMin("80.0") @DecimalMax("250.0")
    BigDecimal heightCm,

    @DecimalMin("20.0") @DecimalMax("350.0")
    BigDecimal weightKg,

    @Size(max = 30)
    String activityLevel,

    @Size(max = 40)
    String dietaryPattern,

    @Size(max = 80)
    String stateOrRegion,

    @Size(max = 5)
    Set<@Size(max = 50) String> goals,

    @Size(max = 12)
    Set<@Size(max = 50) String> allergies,

    @Size(max = 10)
    Set<@Size(max = 60) String> healthContexts
) {
}

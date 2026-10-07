package in.aarogya.administration.api;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateNutritionSourceRequest(
    @NotBlank
    @Size(max = 80)
    String sourceCode,

    @NotBlank
    @Size(max = 180)
    String name,

    @Size(max = 80)
    String versionLabel,

    @NotBlank
    @Size(max = 40)
    String sourceType,

    @Size(max = 500)
    String sourceUrl,

    @Size(max = 120)
    String licenseLabel,

    @Size(max = 1000)
    String usageNote,

    LocalDate retrievedOn
) {
}

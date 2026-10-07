package in.aarogya.administration.api;

import java.time.LocalDate;
import java.util.UUID;

import in.aarogya.nutrition.domain.NutritionSource;

public record AdminSourceResponse(
    UUID id,
    String sourceCode,
    String name,
    String versionLabel,
    String sourceType,
    String sourceUrl,
    String licenseLabel,
    String usageNote,
    LocalDate retrievedOn
) {
    public static AdminSourceResponse from(NutritionSource source) {
        return new AdminSourceResponse(
            source.getId(),
            source.getSourceCode(),
            source.getName(),
            source.getVersionLabel(),
            source.getSourceType(),
            source.getSourceUrl(),
            source.getLicenseLabel(),
            source.getUsageNote(),
            source.getRetrievedOn()
        );
    }
}

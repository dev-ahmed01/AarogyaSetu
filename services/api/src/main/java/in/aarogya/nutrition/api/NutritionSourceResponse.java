package in.aarogya.nutrition.api;

import java.time.LocalDate;

import in.aarogya.nutrition.domain.NutritionSource;

public record NutritionSourceResponse(
    String code,
    String name,
    String version,
    String type,
    String url,
    String license,
    String usageNote,
    LocalDate retrievedOn
) {

    public static NutritionSourceResponse from(NutritionSource source) {
        if (source == null) {
            return null;
        }

        return new NutritionSourceResponse(
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

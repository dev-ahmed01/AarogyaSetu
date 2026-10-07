package in.aarogya.regional.api;

import in.aarogya.nutrition.api.FoodSummaryResponse;

public record RegionalAlternativeResponse(
    String sourceFoodSlug,
    FoodSummaryResponse alternative,
    String matchedRegionCode,
    int priority,
    String rationale,
    boolean planningEligible
) {
}

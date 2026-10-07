package in.aarogya.regional.api;

import java.util.List;

import in.aarogya.nutrition.api.FoodSummaryResponse;

public record RegionalFoodResponse(
    FoodSummaryResponse food,
    int fitScore,
    String fitLabel,
    String matchedRegionCode,
    String relationship,
    String rationale,
    String sourceCode,
    List<LocalizedAliasResponse> localizedAliases,
    boolean planningEligible
) {
}

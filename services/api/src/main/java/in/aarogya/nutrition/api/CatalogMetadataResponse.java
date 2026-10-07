package in.aarogya.nutrition.api;

import java.util.List;

public record CatalogMetadataResponse(
    List<String> categories,
    List<String> dietaryClassifications,
    List<String> regions,
    List<String> nutrientStatuses,
    List<String> allergenCodes,
    List<NutritionSourceResponse> sources
) {
}

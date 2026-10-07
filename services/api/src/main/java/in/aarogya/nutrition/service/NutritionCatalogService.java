package in.aarogya.nutrition.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.nutrition.api.CatalogMetadataResponse;
import in.aarogya.nutrition.api.FoodDetailResponse;
import in.aarogya.nutrition.api.FoodPageResponse;
import in.aarogya.nutrition.api.NutritionSourceResponse;
import in.aarogya.nutrition.repository.FoodIngredientRepository;
import in.aarogya.nutrition.repository.FoodRepository;
import in.aarogya.nutrition.repository.NutritionSourceRepository;

@Service
public class NutritionCatalogService {

    private static final Set<String> ALLERGEN_CODES = Set.of(
        "WHEAT", "CRUSTACEAN", "MILK", "EGG", "FISH",
        "PEANUT", "TREE_NUT", "SOY", "SULPHITE"
    );

    private final FoodRepository foodRepository;
    private final FoodIngredientRepository ingredientRepository;
    private final NutritionSourceRepository sourceRepository;

    public NutritionCatalogService(
        FoodRepository foodRepository,
        FoodIngredientRepository ingredientRepository,
        NutritionSourceRepository sourceRepository
    ) {
        this.foodRepository = foodRepository;
        this.ingredientRepository = ingredientRepository;
        this.sourceRepository = sourceRepository;
    }

    @Transactional(readOnly = true)
    public FoodPageResponse search(
        String query,
        String category,
        String dietary,
        String region,
        String nutrientStatus,
        int page,
        int size
    ) {
        var safePage = Math.max(0, page);
        var safeSize = Math.min(Math.max(size, 1), 50);

        var result = foodRepository.search(
            normalizeText(query),
            normalizeCode(category),
            normalizeCode(dietary),
            normalizeText(region),
            normalizeCode(nutrientStatus),
            PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.ASC, "canonicalName")
            )
        );

        return FoodPageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public FoodDetailResponse getBySlug(String slug) {
        var food = foodRepository.findBySlugAndActiveTrue(slug)
            .orElseThrow(() -> new FoodNotFoundException(slug));

        var ingredients = ingredientRepository
            .findByParentFood_SlugOrderByDisplayOrderAsc(slug);

        return FoodDetailResponse.from(food, ingredients);
    }

    @Transactional(readOnly = true)
    public CatalogMetadataResponse metadata() {
        var sources = sourceRepository.findAllByOrderByNameAsc().stream()
            .map(NutritionSourceResponse::from)
            .toList();

        return new CatalogMetadataResponse(
            foodRepository.findDistinctCategories(),
            foodRepository.findDistinctDietaryClassifications(),
            foodRepository.findDistinctRegions(),
            foodRepository.findDistinctNutrientStatuses(),
            ALLERGEN_CODES.stream().sorted().toList(),
            sources
        );
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}

package in.aarogya.nutrition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import in.aarogya.nutrition.repository.FoodIngredientRepository;
import in.aarogya.nutrition.repository.FoodRepository;
import in.aarogya.nutrition.repository.NutritionSourceRepository;
import in.aarogya.nutrition.service.FoodNotFoundException;
import in.aarogya.nutrition.service.NutritionCatalogService;

class NutritionCatalogServiceTests {

    @Test
    void missingFoodReturnsDomainSpecificNotFound() {
        var foods = Mockito.mock(FoodRepository.class);
        var ingredients = Mockito.mock(FoodIngredientRepository.class);
        var sources = Mockito.mock(NutritionSourceRepository.class);
        var service = new NutritionCatalogService(foods, ingredients, sources);

        when(foods.findBySlugAndActiveTrue("missing-food")).thenReturn(Optional.empty());

        assertThrows(
            FoodNotFoundException.class,
            () -> service.getBySlug("missing-food")
        );
    }

    @Test
    void metadataExposesCatalogTaxonomyAndSources() {
        var foods = Mockito.mock(FoodRepository.class);
        var ingredients = Mockito.mock(FoodIngredientRepository.class);
        var sources = Mockito.mock(NutritionSourceRepository.class);
        var service = new NutritionCatalogService(foods, ingredients, sources);

        when(foods.findDistinctCategories()).thenReturn(List.of("FRUIT", "PULSE"));
        when(foods.findDistinctDietaryClassifications()).thenReturn(List.of("VEGAN"));
        when(foods.findDistinctRegions()).thenReturn(List.of("All India"));
        when(foods.findDistinctNutrientStatuses()).thenReturn(List.of("SOURCE_REFERENCED"));
        when(sources.findAllByOrderByNameAsc()).thenReturn(List.of());

        var metadata = service.metadata();

        assertEquals(List.of("FRUIT", "PULSE"), metadata.categories());
        assertEquals(List.of("VEGAN"), metadata.dietaryClassifications());
        assertEquals(List.of("All India"), metadata.regions());
        assertEquals(List.of("SOURCE_REFERENCED"), metadata.nutrientStatuses());
    }
}

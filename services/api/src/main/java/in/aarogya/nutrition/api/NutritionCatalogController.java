package in.aarogya.nutrition.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.nutrition.service.NutritionCatalogService;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionCatalogController {

    private final NutritionCatalogService catalogService;

    public NutritionCatalogController(NutritionCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/foods")
    FoodPageResponse search(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String dietary,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String nutrientStatus,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return catalogService.search(
            q,
            category,
            dietary,
            region,
            nutrientStatus,
            page,
            size
        );
    }

    @GetMapping("/foods/{slug}")
    FoodDetailResponse get(@PathVariable String slug) {
        return catalogService.getBySlug(slug);
    }

    @GetMapping("/metadata")
    CatalogMetadataResponse metadata() {
        return catalogService.metadata();
    }
}

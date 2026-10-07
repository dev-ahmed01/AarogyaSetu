package in.aarogya.meals.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.meals.service.MealLogService;
import in.aarogya.nutrition.api.FoodSummaryResponse;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/meals")
public class MealLogController {

    private final MealLogService mealLogService;

    public MealLogController(MealLogService mealLogService) {
        this.mealLogService = mealLogService;
    }

    @GetMapping("/day")
    DailyMealLogResponse day(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate date
    ) {
        return mealLogService.day(principal.id(), date);
    }

    @GetMapping("/history")
    MealHistoryResponse history(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to
    ) {
        return mealLogService.history(principal.id(), from, to);
    }

    @PostMapping("/entries")
    MealEntryResponse create(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody MealEntryUpsertRequest request
    ) {
        return mealLogService.create(principal.id(), request);
    }

    @PutMapping("/entries/{entryId}")
    MealEntryResponse update(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID entryId,
        @Valid @RequestBody MealEntryUpsertRequest request
    ) {
        return mealLogService.update(principal.id(), entryId, request);
    }

    @DeleteMapping("/entries/{entryId}")
    ResponseEntity<Void> delete(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID entryId
    ) {
        mealLogService.delete(principal.id(), entryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recent")
    List<FoodSummaryResponse> recent(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(defaultValue = "8") int limit
    ) {
        return mealLogService.recentFoods(principal.id(), limit);
    }

    @GetMapping("/favorites")
    List<FoodSummaryResponse> favorites(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return mealLogService.favorites(principal.id());
    }

    @PutMapping("/favorites/{foodSlug}")
    FoodSummaryResponse addFavorite(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable String foodSlug
    ) {
        return mealLogService.addFavorite(principal.id(), foodSlug);
    }

    @DeleteMapping("/favorites/{foodSlug}")
    ResponseEntity<Void> removeFavorite(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable String foodSlug
    ) {
        mealLogService.removeFavorite(principal.id(), foodSlug);
        return ResponseEntity.noContent().build();
    }
}

package in.aarogya.progress.api;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.progress.service.ProgressService;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/overview")
    ProgressOverviewResponse overview(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate date
    ) {
        return progressService.overview(principal.id(), date);
    }

    @PostMapping("/evaluate")
    ProgressOverviewResponse evaluate(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate date
    ) {
        return progressService.evaluate(principal.id(), date);
    }

    @PutMapping("/goals/meal-logging")
    ProgressOverviewResponse updateMealLoggingGoal(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody GoalUpdateRequest request
    ) {
        return progressService.upsertMealLoggingGoal(
            principal.id(),
            request.targetDaysPerWeek()
        );
    }

    @PostMapping("/goals/meal-logging/pause")
    ProgressOverviewResponse pauseMealLoggingGoal(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return progressService.pauseMealLoggingGoal(principal.id());
    }

    @PostMapping("/goals/meal-logging/resume")
    ProgressOverviewResponse resumeMealLoggingGoal(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return progressService.resumeMealLoggingGoal(principal.id());
    }
}

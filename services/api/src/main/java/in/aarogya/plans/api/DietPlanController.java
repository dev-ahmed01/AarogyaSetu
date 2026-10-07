package in.aarogya.plans.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.plans.service.DietPlanService;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/plans")
public class DietPlanController {

    private final DietPlanService planService;

    public DietPlanController(DietPlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/day")
    PlanDayResponse day(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate date
    ) {
        return planService.current(principal.id(), date);
    }

    @GetMapping("/suggestions")
    List<SmartFoodSuggestionResponse> suggestions(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate date,
        @RequestParam(defaultValue = "6") int limit
    ) {
        return planService.suggestions(principal.id(), date, limit);
    }

    @PostMapping("/generate")
    DietPlanResponse generate(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody GeneratePlanRequest request
    ) {
        return planService.generate(principal.id(), request);
    }

    @DeleteMapping("/{planId}")
    ResponseEntity<Void> archive(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID planId
    ) {
        planService.archive(principal.id(), planId);
        return ResponseEntity.noContent().build();
    }
}

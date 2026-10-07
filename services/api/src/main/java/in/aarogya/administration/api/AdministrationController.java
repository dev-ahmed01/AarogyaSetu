package in.aarogya.administration.api;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.administration.service.AdministrationService;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST')")
public class AdministrationController {

    private final AdministrationService administrationService;

    public AdministrationController(
        AdministrationService administrationService
    ) {
        this.administrationService = administrationService;
    }

    @GetMapping("/overview")
    AdminOverviewResponse overview(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return administrationService.overview(principal);
    }

    @GetMapping("/foods")
    AdminFoodPageResponse foods(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return administrationService.foods(
            q,
            status,
            page,
            size
        );
    }

    @GetMapping("/foods/{foodId}")
    AdminFoodDetailResponse food(
        @PathVariable UUID foodId
    ) {
        return administrationService.food(foodId);
    }

    @PutMapping("/foods/{foodId}/curation")
    AdminFoodDetailResponse saveCuration(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID foodId,
        @Valid @RequestBody FoodCurationRequest request
    ) {
        return administrationService.saveCuration(
            principal,
            foodId,
            request
        );
    }

    @PostMapping("/foods/{foodId}/ready")
    AdminFoodDetailResponse markReady(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID foodId,
        @Valid @RequestBody ReviewNoteRequest request
    ) {
        return administrationService.markReady(
            principal,
            foodId,
            request.note()
        );
    }

    @PostMapping("/foods/{foodId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    AdminFoodDetailResponse publish(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID foodId,
        @Valid @RequestBody ReviewNoteRequest request
    ) {
        return administrationService.publish(
            principal,
            foodId,
            request.note()
        );
    }

    @PostMapping("/foods/{foodId}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    AdminFoodDetailResponse unpublish(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID foodId,
        @Valid @RequestBody ReviewNoteRequest request
    ) {
        return administrationService.unpublish(
            principal,
            foodId,
            request.note()
        );
    }

    @PostMapping("/foods/{foodId}/return")
    @PreAuthorize("hasRole('ADMIN')")
    AdminFoodDetailResponse returnForChanges(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID foodId,
        @Valid @RequestBody ReviewNoteRequest request
    ) {
        return administrationService.returnForChanges(
            principal,
            foodId,
            request.note()
        );
    }

    @GetMapping("/sources")
    List<AdminSourceResponse> sources() {
        return administrationService.sources();
    }

    @PostMapping("/sources")
    @PreAuthorize("hasRole('ADMIN')")
    AdminSourceResponse createSource(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody CreateNutritionSourceRequest request
    ) {
        return administrationService.createSource(
            principal,
            request
        );
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    List<AuditEventResponse> audit(
        @RequestParam(required = false) String eventType,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return administrationService.auditEvents(
            eventType,
            limit
        );
    }
}

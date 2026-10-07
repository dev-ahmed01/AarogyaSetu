package in.aarogya.research.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.research.service.ResearchEvaluationService;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/research")
public class ResearchController {

    private final ResearchEvaluationService researchService;

    public ResearchController(
        ResearchEvaluationService researchService
    ) {
        this.researchService = researchService;
    }

    @GetMapping("/consent")
    ResearchConsentResponse consent(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return researchService.consent(principal.id());
    }

    @PutMapping("/consent")
    ResearchConsentResponse updateConsent(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody ResearchConsentUpdateRequest request
    ) {
        return researchService.updateConsent(
            principal.id(),
            request
        );
    }

    @PostMapping("/events")
    ResearchEventResponse event(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody ResearchEventRequest request
    ) {
        return researchService.recordEvent(
            principal.id(),
            request.eventCode()
        );
    }
}

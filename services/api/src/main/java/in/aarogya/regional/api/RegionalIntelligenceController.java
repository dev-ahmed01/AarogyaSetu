package in.aarogya.regional.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.regional.service.RegionalIntelligenceService;
import in.aarogya.security.AarogyaPrincipal;

@RestController
@RequestMapping("/api/regional")
public class RegionalIntelligenceController {

    private final RegionalIntelligenceService regionalService;

    public RegionalIntelligenceController(
        RegionalIntelligenceService regionalService
    ) {
        this.regionalService = regionalService;
    }

    @GetMapping("/context")
    RegionalContextResponse context(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return regionalService.context(principal.id());
    }

    @GetMapping("/foods")
    List<RegionalFoodResponse> foods(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return regionalService.foods(principal.id(), limit);
    }

    @GetMapping("/foods/{slug}/alternatives")
    List<RegionalAlternativeResponse> alternatives(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable String slug
    ) {
        return regionalService.alternatives(principal.id(), slug);
    }
}

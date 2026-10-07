package in.aarogya.recommendations.api;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.recommendations.service.RecommendationEngineService;
import in.aarogya.security.AarogyaPrincipal;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationEngineService recommendationEngine;

    public RecommendationController(
        RecommendationEngineService recommendationEngine
    ) {
        this.recommendationEngine = recommendationEngine;
    }

    @GetMapping("/today")
    RecommendationAssessmentResponse today(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate date
    ) {
        return recommendationEngine.assess(principal.id(), date);
    }
}

package in.aarogya.analytics.api;

import java.time.LocalDate;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.analytics.service.LongitudinalAnalyticsService;
import in.aarogya.security.AarogyaPrincipal;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final LongitudinalAnalyticsService analyticsService;

    public AnalyticsController(
        LongitudinalAnalyticsService analyticsService
    ) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/longitudinal")
    LongitudinalAnalyticsResponse longitudinal(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate date,
        @RequestParam(defaultValue = "7") int window
    ) {
        return analyticsService.analyze(
            principal.id(),
            date,
            window
        );
    }
}

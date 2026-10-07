package in.aarogya.research.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.research.service.ResearchEvaluationService;
import in.aarogya.security.AarogyaPrincipal;

@RestController
@RequestMapping("/api/admin/research")
@PreAuthorize("hasRole('ADMIN')")
public class AdminResearchController {

    private final ResearchEvaluationService researchService;

    public AdminResearchController(
        ResearchEvaluationService researchService
    ) {
        this.researchService = researchService;
    }

    @GetMapping("/overview")
    ResearchOverviewResponse overview(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to
    ) {
        return researchService.overview(from, to);
    }

    @GetMapping("/metrics")
    List<ResearchMetricDefinitionResponse> metrics() {
        return researchService.definitions();
    }

    @GetMapping(
        value = "/export",
        produces = "text/csv"
    )
    ResponseEntity<String> export(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to
    ) {
        var csv = researchService.exportCsv(
            principal,
            from,
            to
        );

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("text/csv"))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=aarogya-research-evaluation.csv"
            )
            .body(csv);
    }
}

package in.aarogya.nudges.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.nudges.service.NudgeService;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/nudges")
public class NudgeController {

    private final NudgeService nudgeService;

    public NudgeController(NudgeService nudgeService) {
        this.nudgeService = nudgeService;
    }

    @PostMapping("/evaluate")
    NudgeEvaluationResponse evaluate(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) LocalDate date
    ) {
        return nudgeService.evaluate(principal.id(), date);
    }

    @GetMapping
    List<NudgeResponse> list(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(defaultValue = "false") boolean includeHistory
    ) {
        return nudgeService.list(principal.id(), includeHistory);
    }

    @GetMapping("/summary")
    NudgeSummaryResponse summary(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return nudgeService.summary(principal.id());
    }

    @PutMapping("/{nudgeId}/snooze")
    NudgeResponse snooze(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID nudgeId,
        @Valid @RequestBody NudgeSnoozeRequest request
    ) {
        return nudgeService.snooze(
            principal.id(),
            nudgeId,
            request.hours()
        );
    }

    @PutMapping("/{nudgeId}/acknowledge")
    NudgeResponse acknowledge(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID nudgeId
    ) {
        return nudgeService.acknowledge(principal.id(), nudgeId);
    }

    @PutMapping("/{nudgeId}/dismiss")
    NudgeResponse dismiss(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID nudgeId
    ) {
        return nudgeService.dismiss(principal.id(), nudgeId);
    }
}

package in.aarogya.health.api;

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

import in.aarogya.health.service.HealthRecordService;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/health")
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    public HealthRecordController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @GetMapping("/summary")
    HealthSummaryResponse summary(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return healthRecordService.summary(principal.id());
    }

    @GetMapping("/records")
    List<HealthRecordResponse> records(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestParam(required = false) String recordType,
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to
    ) {
        return healthRecordService.list(
            principal.id(),
            recordType,
            from,
            to
        );
    }

    @GetMapping("/records/{recordId}")
    HealthRecordResponse record(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID recordId
    ) {
        return healthRecordService.get(principal.id(), recordId);
    }

    @PostMapping("/records")
    HealthRecordResponse create(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody HealthRecordCreateRequest request
    ) {
        return healthRecordService.createManual(principal.id(), request);
    }

    @DeleteMapping("/records/{recordId}")
    ResponseEntity<Void> delete(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @PathVariable UUID recordId
    ) {
        healthRecordService.delete(principal.id(), recordId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consent")
    HealthConsentResponse consent(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return healthRecordService.consent(principal.id());
    }

    @PutMapping("/consent")
    HealthConsentResponse updateConsent(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @RequestBody HealthConsentUpdateRequest request
    ) {
        return healthRecordService.updateConsent(principal.id(), request);
    }
}

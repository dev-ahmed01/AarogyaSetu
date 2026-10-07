package in.aarogya.health.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.health.service.AbdmIntegrationService;
import in.aarogya.security.AarogyaPrincipal;

@RestController
@RequestMapping("/api/health/integrations/abdm")
public class AbdmIntegrationController {

    private final AbdmIntegrationService integrationService;

    public AbdmIntegrationController(AbdmIntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @GetMapping("/status")
    HealthIntegrationResponse status(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return integrationService.status(principal.id());
    }

    @PostMapping("/connect")
    HealthIntegrationResponse connect(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return integrationService.connect(principal.id());
    }

    @PostMapping("/import")
    HealthImportResponse importRecords(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return integrationService.importRecords(principal.id());
    }

    @DeleteMapping("/connection")
    HealthIntegrationResponse disconnect(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return integrationService.disconnect(principal.id());
    }
}

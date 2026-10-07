package in.aarogya.health;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.health.integration.AbdmHealthRecordAdapter;
import in.aarogya.health.integration.HealthIntegrationDescriptor;
import in.aarogya.health.repository.HealthIntegrationRepository;
import in.aarogya.health.repository.HealthRecordRepository;
import in.aarogya.health.service.AbdmIntegrationService;
import in.aarogya.health.service.HealthIntegrationUnavailableException;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.security.SecurityAuditService;

class AbdmIntegrationServiceTests {

    @Test
    void importRequiresAnExplicitConnectedState() {
        var integrations = mock(HealthIntegrationRepository.class);
        var records = mock(HealthRecordRepository.class);
        var users = mock(UserAccountRepository.class);
        var adapter = mock(AbdmHealthRecordAdapter.class);
        var audit = mock(SecurityAuditService.class);
        var userId = UUID.randomUUID();

        when(adapter.descriptor()).thenReturn(new HealthIntegrationDescriptor(
            "ABDM_MOCK",
            "ABDM demo",
            "MOCK",
            false,
            "FHIR-compatible normalization boundary",
            "Synthetic demo."
        ));
        when(integrations.findByUser_IdAndProviderCode(
            userId,
            "ABDM_MOCK"
        )).thenReturn(Optional.empty());

        var service = new AbdmIntegrationService(
            integrations,
            records,
            users,
            adapter,
            audit
        );

        assertThrows(
            HealthIntegrationUnavailableException.class,
            () -> service.importRecords(userId)
        );

        verifyNoInteractions(records, users, audit);
    }
}

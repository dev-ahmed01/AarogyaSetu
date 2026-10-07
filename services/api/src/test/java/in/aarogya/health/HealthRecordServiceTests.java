package in.aarogya.health;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.health.repository.HealthObservationRepository;
import in.aarogya.health.repository.HealthRecordRepository;
import in.aarogya.health.service.HealthRecordService;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.profile.repository.ConsentRecordRepository;
import in.aarogya.security.SecurityAuditService;

class HealthRecordServiceTests {

    @Test
    void unsupportedRecordTypeStopsBeforeRepositorySearch() {
        var records = mock(HealthRecordRepository.class);
        var observations = mock(HealthObservationRepository.class);
        var consents = mock(ConsentRecordRepository.class);
        var users = mock(UserAccountRepository.class);
        var audit = mock(SecurityAuditService.class);

        var service = new HealthRecordService(
            records,
            observations,
            consents,
            users,
            audit
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.list(
                UUID.randomUUID(),
                "UNSUPPORTED_TYPE",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 7)
            )
        );

        verifyNoInteractions(records);
    }
}

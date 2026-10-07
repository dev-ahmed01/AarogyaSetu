package in.aarogya.health;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import in.aarogya.health.domain.HealthObservation;
import in.aarogya.health.domain.HealthRecord;
import in.aarogya.identity.domain.UserAccount;

import static org.mockito.Mockito.mock;

class HealthRecordDomainTests {

    @Test
    void healthRecordRetainsSourceAndStructuredObservations() {
        var user = mock(UserAccount.class);
        var record = new HealthRecord(
            user,
            "MEASUREMENT_SET",
            "Home measurement",
            "Self-reported measurement.",
            LocalDate.of(2026, 10, 6),
            null,
            null,
            "MANUAL",
            "AAROGYA_MANUAL",
            null,
            null,
            "SELF_REPORTED",
            "Entered manually by the signed-in Aarogya user.",
            null,
            null
        );

        record.addObservation(new HealthObservation(
            record,
            "BODY_WEIGHT",
            "AAROGYA_LOCAL",
            "Body weight",
            new BigDecimal("70.20"),
            null,
            "kg",
            null,
            Instant.parse("2026-10-06T06:00:00Z"),
            null
        ));

        assertEquals("MANUAL", record.getSourceType());
        assertEquals("SELF_REPORTED", record.getVerificationStatus());
        assertEquals(1, record.getObservations().size());
        assertEquals(
            new BigDecimal("70.20"),
            record.getObservations().iterator().next().getValueNumeric()
        );
    }
}

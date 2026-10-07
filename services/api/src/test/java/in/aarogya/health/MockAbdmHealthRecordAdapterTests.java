package in.aarogya.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.health.integration.MockAbdmHealthRecordAdapter;

class MockAbdmHealthRecordAdapterTests {

    private final MockAbdmHealthRecordAdapter adapter =
        new MockAbdmHealthRecordAdapter();

    @Test
    void mockAdapterNeverClaimsLiveConnectivity() {
        var descriptor = adapter.descriptor();

        assertEquals("ABDM_MOCK", descriptor.providerCode());
        assertEquals("MOCK", descriptor.integrationMode());
        assertFalse(descriptor.liveConnectivity());
        assertTrue(descriptor.disclaimer().contains("No live ABDM"));
    }

    @Test
    void mockSubjectReferenceCannotBeMistakenForAnAbhaNumber() {
        var reference = adapter.createExternalSubjectRef(UUID.randomUUID());

        assertTrue(reference.startsWith("DEMO-SUBJECT-"));
        assertFalse(reference.matches("\\d{2}-\\d{4}-\\d{4}-\\d{4}"));
    }

    @Test
    void mockDatasetIsDeterministicAndExplicitlySynthetic() {
        var records = adapter.fetchRecords(UUID.randomUUID());

        assertEquals(3, records.size());
        assertTrue(records.stream()
            .allMatch(record ->
                record.summaryText().toLowerCase().contains("synthetic")
            ));
    }
}

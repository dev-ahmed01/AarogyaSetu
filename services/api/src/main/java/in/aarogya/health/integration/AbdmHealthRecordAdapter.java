package in.aarogya.health.integration;

import java.util.List;
import java.util.UUID;

public interface AbdmHealthRecordAdapter {

    HealthIntegrationDescriptor descriptor();

    String createExternalSubjectRef(UUID userId);

    List<ExternalHealthRecord> fetchRecords(UUID userId);
}

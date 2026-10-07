package in.aarogya.health.integration;

import java.time.LocalDate;
import java.util.List;

public record ExternalHealthRecord(
    String sourceRecordRef,
    String recordType,
    String title,
    String summaryText,
    LocalDate clinicalDate,
    String providerName,
    String facilityName,
    String interoperabilityResourceType,
    List<ExternalHealthObservation> observations
) {
}

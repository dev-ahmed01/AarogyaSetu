package in.aarogya.health.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import in.aarogya.health.domain.HealthObservation;
import in.aarogya.health.domain.HealthRecord;

public record HealthRecordResponse(
    UUID id,
    String recordType,
    String title,
    String summaryText,
    LocalDate clinicalDate,
    String providerName,
    String facilityName,
    String sourceType,
    String sourceSystem,
    String sourceRecordRef,
    String interoperabilityResourceType,
    String verificationStatus,
    String provenanceLabel,
    Instant importedAt,
    Instant createdAt,
    List<HealthObservationResponse> observations
) {

    public static HealthRecordResponse from(HealthRecord record) {
        return new HealthRecordResponse(
            record.getId(),
            record.getRecordType(),
            record.getTitle(),
            record.getSummaryText(),
            record.getClinicalDate(),
            record.getProviderName(),
            record.getFacilityName(),
            record.getSourceType(),
            record.getSourceSystem(),
            record.getSourceRecordRef(),
            record.getInteroperabilityResourceType(),
            record.getVerificationStatus(),
            record.getProvenanceLabel(),
            record.getImportedAt(),
            record.getCreatedAt(),
            record.getObservations().stream()
                .sorted(
                    Comparator
                        .comparing(
                            HealthObservation::getObservedAt,
                            Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(HealthObservation::getDisplayName)
                )
                .map(HealthObservationResponse::from)
                .toList()
        );
    }
}

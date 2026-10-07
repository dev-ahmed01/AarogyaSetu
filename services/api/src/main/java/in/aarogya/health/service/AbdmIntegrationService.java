package in.aarogya.health.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.health.api.HealthImportResponse;
import in.aarogya.health.api.HealthIntegrationResponse;
import in.aarogya.health.domain.HealthIntegration;
import in.aarogya.health.domain.HealthObservation;
import in.aarogya.health.domain.HealthRecord;
import in.aarogya.health.integration.AbdmHealthRecordAdapter;
import in.aarogya.health.integration.ExternalHealthRecord;
import in.aarogya.health.repository.HealthIntegrationRepository;
import in.aarogya.health.repository.HealthRecordRepository;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.security.SecurityAuditService;

@Service
public class AbdmIntegrationService {

    private final HealthIntegrationRepository integrationRepository;
    private final HealthRecordRepository recordRepository;
    private final UserAccountRepository userRepository;
    private final AbdmHealthRecordAdapter adapter;
    private final SecurityAuditService auditService;

    public AbdmIntegrationService(
        HealthIntegrationRepository integrationRepository,
        HealthRecordRepository recordRepository,
        UserAccountRepository userRepository,
        AbdmHealthRecordAdapter adapter,
        SecurityAuditService auditService
    ) {
        this.integrationRepository = integrationRepository;
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
        this.adapter = adapter;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public HealthIntegrationResponse status(UUID userId) {
        var descriptor = adapter.descriptor();
        var integration = integrationRepository
            .findByUser_IdAndProviderCode(userId, descriptor.providerCode())
            .orElse(null);

        return toResponse(integration);
    }

    @Transactional
    public HealthIntegrationResponse connect(UUID userId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        var descriptor = adapter.descriptor();

        var integration = integrationRepository
            .findByUser_IdAndProviderCode(userId, descriptor.providerCode())
            .orElseGet(() -> new HealthIntegration(
                user,
                descriptor.providerCode(),
                descriptor.displayName(),
                descriptor.integrationMode(),
                descriptor.liveConnectivity(),
                descriptor.interoperabilityStandard()
            ));

        integration.connect(adapter.createExternalSubjectRef(userId));
        integrationRepository.save(integration);

        auditService.record(
            user,
            "HEALTH_INTEGRATION_CONNECTED",
            "SUCCESS",
            descriptor.providerCode(),
            "mode=MOCK;liveConnectivity=false"
        );

        return toResponse(integration);
    }

    @Transactional
    public HealthImportResponse importRecords(UUID userId) {
        var descriptor = adapter.descriptor();
        var integration = integrationRepository
            .findByUser_IdAndProviderCode(userId, descriptor.providerCode())
            .filter(item -> "CONNECTED".equals(item.getStatus()))
            .orElseThrow(() -> new HealthIntegrationUnavailableException(
                "Connect the mock ABDM source before importing demo records."
            ));
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        var imported = 0;
        var skipped = 0;

        for (var external : adapter.fetchRecords(userId)) {
            if (recordRepository.existsByUser_IdAndSourceSystemAndSourceRecordRef(
                userId,
                descriptor.providerCode(),
                external.sourceRecordRef()
            )) {
                skipped++;
                continue;
            }

            var record = toRecord(user, descriptor.providerCode(), external);
            recordRepository.save(record);
            imported++;
        }

        integration.markImported();
        integrationRepository.save(integration);

        auditService.record(
            user,
            "HEALTH_RECORDS_IMPORTED",
            "SUCCESS",
            descriptor.providerCode(),
            "imported=" + imported + ";skippedExisting=" + skipped
        );

        return new HealthImportResponse(
            imported,
            skipped,
            integration.getLastImportedAt(),
            descriptor.providerCode(),
            descriptor.liveConnectivity(),
            imported == 0
                ? "No new demo records were available."
                : "Synthetic demo records were imported into Aarogya's local health-record store."
        );
    }

    @Transactional
    public HealthIntegrationResponse disconnect(UUID userId) {
        var descriptor = adapter.descriptor();
        var integration = integrationRepository
            .findByUser_IdAndProviderCode(userId, descriptor.providerCode())
            .orElseThrow(() -> new HealthIntegrationUnavailableException(
                "No mock ABDM connection exists."
            ));
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        integration.disconnect();

        auditService.record(
            user,
            "HEALTH_INTEGRATION_DISCONNECTED",
            "SUCCESS",
            descriptor.providerCode(),
            "localRecordsRetained=true"
        );

        return toResponse(integration);
    }

    private HealthRecord toRecord(
        in.aarogya.identity.domain.UserAccount user,
        String sourceSystem,
        ExternalHealthRecord external
    ) {
        var record = new HealthRecord(
            user,
            external.recordType(),
            external.title(),
            external.summaryText(),
            external.clinicalDate(),
            external.providerName(),
            external.facilityName(),
            "ABDM_MOCK",
            sourceSystem,
            external.sourceRecordRef(),
            external.interoperabilityResourceType(),
            "MOCK_IMPORTED",
            "Synthetic record imported from the local ABDM/ABHA mock adapter. No live government source was contacted.",
            hash(external),
            Instant.now()
        );

        for (var observation : external.observations()) {
            record.addObservation(new HealthObservation(
                record,
                observation.observationCode(),
                observation.codingSystem(),
                observation.displayName(),
                observation.valueNumeric(),
                observation.valueText(),
                observation.unit(),
                observation.referenceRangeText(),
                observation.observedAt(),
                observation.sourceObservationRef()
            ));
        }

        return record;
    }

    private String hash(ExternalHealthRecord external) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                digest.digest(
                    external.toString().getBytes(StandardCharsets.UTF_8)
                )
            );
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    private HealthIntegrationResponse toResponse(HealthIntegration integration) {
        var descriptor = adapter.descriptor();

        if (integration == null) {
            return new HealthIntegrationResponse(
                descriptor.providerCode(),
                descriptor.displayName(),
                descriptor.integrationMode(),
                "DISCONNECTED",
                descriptor.liveConnectivity(),
                descriptor.interoperabilityStandard(),
                null,
                null,
                null,
                null,
                descriptor.disclaimer()
            );
        }

        return new HealthIntegrationResponse(
            integration.getProviderCode(),
            integration.getDisplayName(),
            integration.getIntegrationMode(),
            integration.getStatus(),
            integration.isLiveConnectivity(),
            integration.getInteroperabilityStandard(),
            integration.getExternalSubjectRef(),
            integration.getConnectedAt(),
            integration.getDisconnectedAt(),
            integration.getLastImportedAt(),
            descriptor.disclaimer()
        );
    }
}

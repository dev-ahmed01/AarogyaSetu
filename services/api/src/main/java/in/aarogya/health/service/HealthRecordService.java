package in.aarogya.health.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.health.api.HealthConsentResponse;
import in.aarogya.health.api.HealthConsentUpdateRequest;
import in.aarogya.health.api.HealthObservationRequest;
import in.aarogya.health.api.HealthRecordCreateRequest;
import in.aarogya.health.api.HealthRecordResponse;
import in.aarogya.health.api.HealthSummaryResponse;
import in.aarogya.health.domain.HealthObservation;
import in.aarogya.health.domain.HealthRecord;
import in.aarogya.health.repository.HealthObservationRepository;
import in.aarogya.health.repository.HealthRecordRepository;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.profile.domain.ConsentRecord;
import in.aarogya.profile.repository.ConsentRecordRepository;
import in.aarogya.security.SecurityAuditService;

@Service
public class HealthRecordService {

    public static final String HEALTH_RECORD_ANALYSIS_CONSENT =
        "HEALTH_RECORD_ANALYSIS";
    public static final String HEALTH_CONSENT_POLICY_VERSION =
        "2026-10-health-v1";

    private static final Set<String> RECORD_TYPES = Set.of(
        "LAB_REPORT",
        "PRESCRIPTION",
        "DISCHARGE_SUMMARY",
        "OP_CONSULT",
        "IMMUNIZATION",
        "MEASUREMENT_SET",
        "OTHER"
    );

    private final HealthRecordRepository recordRepository;
    private final HealthObservationRepository observationRepository;
    private final ConsentRecordRepository consentRepository;
    private final UserAccountRepository userRepository;
    private final SecurityAuditService auditService;

    public HealthRecordService(
        HealthRecordRepository recordRepository,
        HealthObservationRepository observationRepository,
        ConsentRecordRepository consentRepository,
        UserAccountRepository userRepository,
        SecurityAuditService auditService
    ) {
        this.recordRepository = recordRepository;
        this.observationRepository = observationRepository;
        this.consentRepository = consentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public HealthSummaryResponse summary(UUID userId) {
        var latest = recordRepository
            .findTopByUser_IdOrderByClinicalDateDescCreatedAtDesc(userId)
            .map(HealthRecord::getClinicalDate)
            .orElse(null);

        return new HealthSummaryResponse(
            recordRepository.countByUser_Id(userId),
            observationRepository.countByHealthRecord_User_Id(userId),
            latest,
            consent(userId)
        );
    }

    @Transactional(readOnly = true)
    public List<HealthRecordResponse> list(
        UUID userId,
        String recordType,
        LocalDate from,
        LocalDate to
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                "Health-record start date must be on or before end date."
            );
        }

        var normalizedType = normalizeOptionalCode(recordType);

        if (normalizedType != null && !RECORD_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("Unsupported health record type.");
        }

        return recordRepository.search(
            userId,
            normalizedType,
            from,
            to
        ).stream().map(HealthRecordResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public HealthRecordResponse get(UUID userId, UUID recordId) {
        return HealthRecordResponse.from(
            recordRepository.findByIdAndUser_Id(recordId, userId)
                .orElseThrow(HealthRecordNotFoundException::new)
        );
    }

    @Transactional
    public HealthRecordResponse createManual(
        UUID userId,
        HealthRecordCreateRequest request
    ) {
        if (request.clinicalDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Health records cannot be dated in the future."
            );
        }

        var recordType = normalizeOptionalCode(request.recordType());

        if (!RECORD_TYPES.contains(recordType)) {
            throw new IllegalArgumentException("Unsupported health record type.");
        }

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        var record = new HealthRecord(
            user,
            recordType,
            request.title().trim(),
            normalizeOptional(request.summaryText()),
            request.clinicalDate(),
            normalizeOptional(request.providerName()),
            normalizeOptional(request.facilityName()),
            "MANUAL",
            "AAROGYA_MANUAL",
            null,
            null,
            "SELF_REPORTED",
            "Entered manually by the signed-in Aarogya user.",
            null,
            null
        );

        for (var observation : safeObservations(request.observations())) {
            validateObservation(observation);
            record.addObservation(new HealthObservation(
                record,
                normalizeRequiredCode(observation.code()),
                "AAROGYA_LOCAL",
                observation.displayName().trim(),
                observation.valueNumeric(),
                normalizeOptional(observation.valueText()),
                normalizeOptional(observation.unit()),
                normalizeOptional(observation.referenceRangeText()),
                observation.observedAt(),
                null
            ));
        }

        var saved = recordRepository.save(record);

        auditService.record(
            user,
            "HEALTH_RECORD_CREATED",
            "SUCCESS",
            "health-record:" + saved.getId(),
            "source=MANUAL"
        );

        return HealthRecordResponse.from(saved);
    }

    @Transactional
    public void delete(UUID userId, UUID recordId) {
        var record = recordRepository.findByIdAndUser_Id(recordId, userId)
            .orElseThrow(HealthRecordNotFoundException::new);
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        recordRepository.delete(record);

        auditService.record(
            user,
            "HEALTH_RECORD_DELETED",
            "SUCCESS",
            "health-record:" + recordId,
            "localCopyOnly=true"
        );
    }

    @Transactional(readOnly = true)
    public HealthConsentResponse consent(UUID userId) {
        return consentRepository
            .findTopByUser_IdAndConsentTypeOrderByRecordedAtDesc(
                userId,
                HEALTH_RECORD_ANALYSIS_CONSENT
            )
            .map(this::toConsentResponse)
            .orElse(new HealthConsentResponse(
                HEALTH_RECORD_ANALYSIS_CONSENT,
                false,
                null,
                null
            ));
    }

    @Transactional
    public HealthConsentResponse updateConsent(
        UUID userId,
        HealthConsentUpdateRequest request
    ) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        var record = consentRepository.save(new ConsentRecord(
            user,
            HEALTH_RECORD_ANALYSIS_CONSENT,
            request.granted(),
            HEALTH_CONSENT_POLICY_VERSION
        ));

        auditService.record(
            user,
            "HEALTH_RECORD_CONSENT_CHANGED",
            "SUCCESS",
            HEALTH_RECORD_ANALYSIS_CONSENT,
            request.granted()
                ? "granted=true"
                : "granted=false"
        );

        return toConsentResponse(record);
    }

    private HealthConsentResponse toConsentResponse(ConsentRecord record) {
        return new HealthConsentResponse(
            record.getConsentType(),
            record.isGranted(),
            record.getPolicyVersion(),
            record.getRecordedAt()
        );
    }

    private void validateObservation(HealthObservationRequest observation) {
        if (observation.code() == null || observation.code().isBlank()) {
            throw new IllegalArgumentException(
                "Observation code is required."
            );
        }

        if (observation.displayName() == null
            || observation.displayName().isBlank()) {
            throw new IllegalArgumentException(
                "Observation display name is required."
            );
        }

        if (observation.valueNumeric() == null
            && (observation.valueText() == null
                || observation.valueText().isBlank())) {
            throw new IllegalArgumentException(
                "Observation must include a numeric or text value."
            );
        }
    }

    private List<HealthObservationRequest> safeObservations(
        List<HealthObservationRequest> observations
    ) {
        return observations == null ? List.of() : observations;
    }

    private String normalizeOptional(String value) {
        if (value == null) return null;
        var trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeOptionalCode(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredCode(String value) {
        return value.trim()
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9_]+", "_");
    }
}

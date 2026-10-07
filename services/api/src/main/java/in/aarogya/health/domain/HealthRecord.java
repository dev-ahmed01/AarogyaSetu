package in.aarogya.health.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import in.aarogya.identity.domain.UserAccount;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "health_records")
public class HealthRecord {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "record_type", nullable = false, length = 50)
    private String recordType;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(name = "summary_text", length = 1600)
    private String summaryText;

    @Column(name = "clinical_date", nullable = false)
    private LocalDate clinicalDate;

    @Column(name = "provider_name", length = 220)
    private String providerName;

    @Column(name = "facility_name", length = 220)
    private String facilityName;

    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    @Column(name = "source_system", nullable = false, length = 80)
    private String sourceSystem;

    @Column(name = "source_record_ref", length = 160)
    private String sourceRecordRef;

    @Column(name = "interoperability_resource_type", length = 80)
    private String interoperabilityResourceType;

    @Column(name = "verification_status", nullable = false, length = 40)
    private String verificationStatus;

    @Column(name = "provenance_label", nullable = false, length = 300)
    private String provenanceLabel;

    @Column(name = "source_payload_hash", length = 64)
    private String sourcePayloadHash;

    @Column(name = "imported_at")
    private Instant importedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
        mappedBy = "healthRecord",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<HealthObservation> observations = new LinkedHashSet<>();

    protected HealthRecord() {
    }

    public HealthRecord(
        UserAccount user,
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
        String sourcePayloadHash,
        Instant importedAt
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.recordType = recordType;
        this.title = title;
        this.summaryText = summaryText;
        this.clinicalDate = clinicalDate;
        this.providerName = providerName;
        this.facilityName = facilityName;
        this.sourceType = sourceType;
        this.sourceSystem = sourceSystem;
        this.sourceRecordRef = sourceRecordRef;
        this.interoperabilityResourceType = interoperabilityResourceType;
        this.verificationStatus = verificationStatus;
        this.provenanceLabel = provenanceLabel;
        this.sourcePayloadHash = sourcePayloadHash;
        this.importedAt = importedAt;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public void addObservation(HealthObservation observation) {
        observations.add(observation);
    }

    public UUID getId() { return id; }
    public String getRecordType() { return recordType; }
    public String getTitle() { return title; }
    public String getSummaryText() { return summaryText; }
    public LocalDate getClinicalDate() { return clinicalDate; }
    public String getProviderName() { return providerName; }
    public String getFacilityName() { return facilityName; }
    public String getSourceType() { return sourceType; }
    public String getSourceSystem() { return sourceSystem; }
    public String getSourceRecordRef() { return sourceRecordRef; }
    public String getInteroperabilityResourceType() { return interoperabilityResourceType; }
    public String getVerificationStatus() { return verificationStatus; }
    public String getProvenanceLabel() { return provenanceLabel; }
    public String getSourcePayloadHash() { return sourcePayloadHash; }
    public Instant getImportedAt() { return importedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Set<HealthObservation> getObservations() { return Set.copyOf(observations); }
}

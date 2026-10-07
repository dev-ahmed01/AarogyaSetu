package in.aarogya.health.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "health_record_observations")
public class HealthObservation {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "health_record_id", nullable = false)
    private HealthRecord healthRecord;

    @Column(name = "observation_code", nullable = false, length = 100)
    private String observationCode;

    @Column(name = "coding_system", nullable = false, length = 120)
    private String codingSystem;

    @Column(name = "display_name", nullable = false, length = 180)
    private String displayName;

    @Column(name = "value_numeric", precision = 16, scale = 4)
    private BigDecimal valueNumeric;

    @Column(name = "value_text", length = 500)
    private String valueText;

    @Column(length = 40)
    private String unit;

    @Column(name = "reference_range_text", length = 160)
    private String referenceRangeText;

    @Column(name = "observed_at")
    private Instant observedAt;

    @Column(name = "source_observation_ref", length = 160)
    private String sourceObservationRef;

    protected HealthObservation() {
    }

    public HealthObservation(
        HealthRecord healthRecord,
        String observationCode,
        String codingSystem,
        String displayName,
        BigDecimal valueNumeric,
        String valueText,
        String unit,
        String referenceRangeText,
        Instant observedAt,
        String sourceObservationRef
    ) {
        this.id = UUID.randomUUID();
        this.healthRecord = healthRecord;
        this.observationCode = observationCode;
        this.codingSystem = codingSystem;
        this.displayName = displayName;
        this.valueNumeric = valueNumeric;
        this.valueText = valueText;
        this.unit = unit;
        this.referenceRangeText = referenceRangeText;
        this.observedAt = observedAt;
        this.sourceObservationRef = sourceObservationRef;
    }

    public UUID getId() { return id; }
    public String getObservationCode() { return observationCode; }
    public String getCodingSystem() { return codingSystem; }
    public String getDisplayName() { return displayName; }
    public BigDecimal getValueNumeric() { return valueNumeric; }
    public String getValueText() { return valueText; }
    public String getUnit() { return unit; }
    public String getReferenceRangeText() { return referenceRangeText; }
    public Instant getObservedAt() { return observedAt; }
    public String getSourceObservationRef() { return sourceObservationRef; }
}

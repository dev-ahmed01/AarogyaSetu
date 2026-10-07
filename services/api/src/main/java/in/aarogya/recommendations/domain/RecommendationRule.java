package in.aarogya.recommendations.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_rules")
public class RecommendationRule {

    @Id
    private UUID id;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_version", nullable = false)
    private int ruleVersion;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;

    @Column(name = "nutrient_code", length = 60)
    private String nutrientCode;

    @Column(name = "reference_value", precision = 12, scale = 4)
    private BigDecimal referenceValue;

    @Column(name = "reference_unit", length = 40)
    private String referenceUnit;

    @Column(name = "trigger_ratio", precision = 6, scale = 4)
    private BigDecimal triggerRatio;

    @Column(name = "minimum_observed_days", nullable = false)
    private int minimumObservedDays;

    @Column(name = "minimum_age_years")
    private Integer minimumAgeYears;

    @Column(name = "maximum_age_years")
    private Integer maximumAgeYears;

    @Column(name = "reason_code", nullable = false, length = 100)
    private String reasonCode;

    @Column(name = "safety_class", nullable = false, length = 50)
    private String safetyClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_source_id")
    private RecommendationEvidenceSource evidenceSource;

    @Column(nullable = false)
    private boolean active;

    protected RecommendationRule() {
    }

    public String getRuleCode() { return ruleCode; }
    public int getRuleVersion() { return ruleVersion; }
    public String getTitle() { return title; }
    public String getRuleType() { return ruleType; }
    public String getNutrientCode() { return nutrientCode; }
    public BigDecimal getReferenceValue() { return referenceValue; }
    public String getReferenceUnit() { return referenceUnit; }
    public BigDecimal getTriggerRatio() { return triggerRatio; }
    public int getMinimumObservedDays() { return minimumObservedDays; }
    public Integer getMinimumAgeYears() { return minimumAgeYears; }
    public Integer getMaximumAgeYears() { return maximumAgeYears; }
    public String getReasonCode() { return reasonCode; }
    public String getSafetyClass() { return safetyClass; }
    public RecommendationEvidenceSource getEvidenceSource() { return evidenceSource; }
    public boolean isActive() { return active; }
}

package in.aarogya.research.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "research_metric_definitions")
@IdClass(ResearchMetricDefinitionId.class)
public class ResearchMetricDefinition {

    @Id
    @Column(name = "metric_code", nullable = false, length = 100)
    private String metricCode;

    @Id
    @Column(name = "metric_version", nullable = false)
    private int metricVersion;

    @Column(nullable = false, length = 220)
    private String name;

    @Column(nullable = false, length = 1200)
    private String description;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(name = "window_days")
    private Integer windowDays;

    @Column(name = "minimum_cohort_size", nullable = false)
    private int minimumCohortSize;

    protected ResearchMetricDefinition() {
    }

    public String getMetricCode() { return metricCode; }
    public int getMetricVersion() { return metricVersion; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUnit() { return unit; }
    public Integer getWindowDays() { return windowDays; }
    public int getMinimumCohortSize() { return minimumCohortSize; }
}

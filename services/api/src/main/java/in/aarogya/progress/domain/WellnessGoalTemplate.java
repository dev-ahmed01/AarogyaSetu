package in.aarogya.progress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "wellness_goal_templates")
public class WellnessGoalTemplate {

    @Id
    @Column(name = "goal_code", length = 80)
    private String goalCode;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 700)
    private String description;

    @Column(name = "metric_code", nullable = false, length = 80)
    private String metricCode;

    @Column(name = "default_target", nullable = false)
    private int defaultTarget;

    @Column(name = "min_target", nullable = false)
    private int minTarget;

    @Column(name = "max_target", nullable = false)
    private int maxTarget;

    @Column(name = "period_code", nullable = false, length = 30)
    private String periodCode;

    @Column(nullable = false)
    private boolean active;

    protected WellnessGoalTemplate() {}

    public String getGoalCode() { return goalCode; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getMetricCode() { return metricCode; }
    public int getDefaultTarget() { return defaultTarget; }
    public int getMinTarget() { return minTarget; }
    public int getMaxTarget() { return maxTarget; }
    public String getPeriodCode() { return periodCode; }
    public boolean isActive() { return active; }
}

package in.aarogya.progress.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "achievement_definitions")
public class AchievementDefinition {

    @Id
    @Column(name = "achievement_code", length = 100)
    private String achievementCode;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 700)
    private String description;

    @Column(name = "criteria_code", nullable = false, length = 100)
    private String criteriaCode;

    @Column(name = "threshold_value", nullable = false)
    private int thresholdValue;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active;

    protected AchievementDefinition() {}

    public String getAchievementCode() { return achievementCode; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCriteriaCode() { return criteriaCode; }
    public int getThresholdValue() { return thresholdValue; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
}

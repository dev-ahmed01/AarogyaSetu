package in.aarogya.nudges.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nudge_rules")
public class NudgeRule {

    @Id
    private UUID id;

    @Column(name = "rule_code", nullable = false, length = 100)
    private String ruleCode;

    @Column(name = "rule_version", nullable = false)
    private int ruleVersion;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 30)
    private String severity;

    @Column(name = "cooldown_hours", nullable = false)
    private int cooldownHours;

    @Column(nullable = false)
    private boolean active;

    protected NudgeRule() {
    }

    public String getRuleCode() { return ruleCode; }
    public int getRuleVersion() { return ruleVersion; }
    public String getCategory() { return category; }
    public String getSeverity() { return severity; }
    public int getCooldownHours() { return cooldownHours; }
    public boolean isActive() { return active; }
}

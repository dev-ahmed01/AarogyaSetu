package in.aarogya.plans.domain;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "diet_plans")
public class DietPlan {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "generation_mode", nullable = false, length = 50)
    private String generationMode;

    @Column(name = "engine_status", nullable = false, length = 60)
    private String engineStatus;

    @Column(name = "source_rule_code", length = 100)
    private String sourceRuleCode;

    @Column(name = "source_rule_version")
    private Integer sourceRuleVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
        mappedBy = "plan",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<DietPlanItem> items = new LinkedHashSet<>();

    protected DietPlan() {
    }

    public DietPlan(
        UserAccount user,
        LocalDate planDate,
        String generationMode,
        String engineStatus,
        String sourceRuleCode,
        Integer sourceRuleVersion
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.planDate = planDate;
        this.status = "DRAFT";
        this.generationMode = generationMode;
        this.engineStatus = engineStatus;
        this.sourceRuleCode = sourceRuleCode;
        this.sourceRuleVersion = sourceRuleVersion;
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addItem(DietPlanItem item) {
        items.add(item);
    }

    public void archive() {
        this.status = "ARCHIVED";
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public LocalDate getPlanDate() { return planDate; }
    public String getStatus() { return status; }
    public String getGenerationMode() { return generationMode; }
    public String getEngineStatus() { return engineStatus; }
    public String getSourceRuleCode() { return sourceRuleCode; }
    public Integer getSourceRuleVersion() { return sourceRuleVersion; }
    public Instant getCreatedAt() { return createdAt; }
    public Set<DietPlanItem> getItems() { return Set.copyOf(items); }
}

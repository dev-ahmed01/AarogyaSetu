package in.aarogya.progress.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import in.aarogya.identity.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_wellness_goals")
public class UserWellnessGoal {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "goal_code", referencedColumnName = "goal_code", nullable = false)
    private WellnessGoalTemplate template;

    @Column(name = "target_value", nullable = false)
    private int targetValue;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "started_on", nullable = false)
    private LocalDate startedOn;

    @Column(name = "ended_on")
    private LocalDate endedOn;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserWellnessGoal() {}

    public UserWellnessGoal(
        UserAccount user,
        WellnessGoalTemplate template,
        int targetValue,
        LocalDate startedOn
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.template = template;
        this.targetValue = targetValue;
        this.status = "ACTIVE";
        this.startedOn = startedOn;
    }

    @PrePersist
    void onCreate() {
        var now = Instant.now();
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = now;
        if (startedOn == null) startedOn = LocalDate.now();
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public void updateTarget(int targetValue) {
        this.targetValue = targetValue;
        this.status = "ACTIVE";
        this.endedOn = null;
    }

    public void pause() { this.status = "PAUSED"; }

    public void resume() {
        this.status = "ACTIVE";
        this.endedOn = null;
    }

    public UUID getId() { return id; }
    public WellnessGoalTemplate getTemplate() { return template; }
    public int getTargetValue() { return targetValue; }
    public String getStatus() { return status; }
    public LocalDate getStartedOn() { return startedOn; }
    public LocalDate getEndedOn() { return endedOn; }
}

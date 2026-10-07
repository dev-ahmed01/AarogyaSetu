package in.aarogya.progress.domain;

import java.time.Instant;
import java.util.UUID;

import in.aarogya.identity.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_achievements")
public class UserAchievement {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "achievement_code", referencedColumnName = "achievement_code", nullable = false)
    private AchievementDefinition definition;

    @Column(name = "earned_at", nullable = false)
    private Instant earnedAt;

    @Column(name = "evidence_value", nullable = false)
    private int evidenceValue;

    protected UserAchievement() {}

    public UserAchievement(
        UserAccount user,
        AchievementDefinition definition,
        int evidenceValue
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.definition = definition;
        this.evidenceValue = evidenceValue;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (earnedAt == null) earnedAt = Instant.now();
    }

    public AchievementDefinition getDefinition() { return definition; }
    public Instant getEarnedAt() { return earnedAt; }
    public int getEvidenceValue() { return evidenceValue; }
}

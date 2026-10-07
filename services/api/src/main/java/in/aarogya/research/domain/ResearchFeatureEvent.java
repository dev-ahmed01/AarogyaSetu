package in.aarogya.research.domain;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "research_feature_events")
public class ResearchFeatureEvent {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "event_code", nullable = false, length = 80)
    private String eventCode;

    @Column(name = "event_version", nullable = false)
    private int eventVersion;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ResearchFeatureEvent() {
    }

    public ResearchFeatureEvent(
        UserAccount user,
        String eventCode,
        int eventVersion,
        LocalDate eventDate
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.eventCode = eventCode;
        this.eventVersion = eventVersion;
        this.eventDate = eventDate;
        this.occurredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UserAccount getUser() { return user; }
    public String getEventCode() { return eventCode; }
    public int getEventVersion() { return eventVersion; }
    public LocalDate getEventDate() { return eventDate; }
    public Instant getOccurredAt() { return occurredAt; }
}

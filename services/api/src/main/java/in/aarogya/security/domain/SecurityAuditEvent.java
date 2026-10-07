package in.aarogya.security.domain;

import java.time.Instant;

import in.aarogya.identity.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "security_audit_events")
public class SecurityAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "event_outcome", nullable = false, length = 30)
    private String eventOutcome;

    @Column(length = 320)
    private String subject;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "metadata_json")
    private String metadataJson;

    protected SecurityAuditEvent() {
    }

    public SecurityAuditEvent(
        UserAccount user,
        String eventType,
        String eventOutcome,
        String subject,
        String metadataJson
    ) {
        this.user = user;
        this.eventType = eventType;
        this.eventOutcome = eventOutcome;
        this.subject = subject;
        this.metadataJson = metadataJson;
        this.occurredAt = Instant.now();
    }

    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public String getEventType() { return eventType; }
    public String getEventOutcome() { return eventOutcome; }
    public String getSubject() { return subject; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getMetadataJson() { return metadataJson; }
}

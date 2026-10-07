package in.aarogya.profile.domain;

import java.time.Instant;
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
@Table(name = "consent_records")
public class ConsentRecord {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "consent_type", nullable = false, length = 60)
    private String consentType;

    @Column(nullable = false)
    private boolean granted;

    @Column(name = "policy_version", nullable = false, length = 30)
    private String policyVersion;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected ConsentRecord() {
    }

    public ConsentRecord(
        UserAccount user,
        String consentType,
        boolean granted,
        String policyVersion
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.consentType = consentType;
        this.granted = granted;
        this.policyVersion = policyVersion;
        this.recordedAt = Instant.now();
    }

    public boolean isGranted() { return granted; }
    public String getConsentType() { return consentType; }
    public String getPolicyVersion() { return policyVersion; }
    public Instant getRecordedAt() { return recordedAt; }
}

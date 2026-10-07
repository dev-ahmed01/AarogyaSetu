package in.aarogya.health.domain;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "health_integrations")
public class HealthIntegration {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(name = "provider_code", nullable = false, length = 80)
    private String providerCode;

    @Column(name = "display_name", nullable = false, length = 180)
    private String displayName;

    @Column(name = "integration_mode", nullable = false, length = 30)
    private String integrationMode;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "live_connectivity", nullable = false)
    private boolean liveConnectivity;

    @Column(name = "interoperability_standard", length = 120)
    private String interoperabilityStandard;

    @Column(name = "external_subject_ref", length = 180)
    private String externalSubjectRef;

    @Column(name = "connected_at")
    private Instant connectedAt;

    @Column(name = "disconnected_at")
    private Instant disconnectedAt;

    @Column(name = "last_imported_at")
    private Instant lastImportedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected HealthIntegration() {
    }

    public HealthIntegration(
        UserAccount user,
        String providerCode,
        String displayName,
        String integrationMode,
        boolean liveConnectivity,
        String interoperabilityStandard
    ) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.providerCode = providerCode;
        this.displayName = displayName;
        this.integrationMode = integrationMode;
        this.status = "DISCONNECTED";
        this.liveConnectivity = liveConnectivity;
        this.interoperabilityStandard = interoperabilityStandard;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void connect(String externalSubjectRef) {
        this.status = "CONNECTED";
        this.externalSubjectRef = externalSubjectRef;
        this.connectedAt = Instant.now();
        this.disconnectedAt = null;
        this.updatedAt = Instant.now();
    }

    public void disconnect() {
        this.status = "DISCONNECTED";
        this.externalSubjectRef = null;
        this.disconnectedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markImported() {
        this.lastImportedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getProviderCode() { return providerCode; }
    public String getDisplayName() { return displayName; }
    public String getIntegrationMode() { return integrationMode; }
    public String getStatus() { return status; }
    public boolean isLiveConnectivity() { return liveConnectivity; }
    public String getInteroperabilityStandard() { return interoperabilityStandard; }
    public String getExternalSubjectRef() { return externalSubjectRef; }
    public Instant getConnectedAt() { return connectedAt; }
    public Instant getDisconnectedAt() { return disconnectedAt; }
    public Instant getLastImportedAt() { return lastImportedAt; }
}

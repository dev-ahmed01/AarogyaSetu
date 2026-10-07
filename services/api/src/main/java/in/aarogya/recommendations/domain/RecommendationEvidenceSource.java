package in.aarogya.recommendations.domain;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_evidence_sources")
public class RecommendationEvidenceSource {

    @Id
    private UUID id;

    @Column(name = "source_code", nullable = false, unique = true, length = 100)
    private String sourceCode;

    @Column(nullable = false, length = 220)
    private String name;

    @Column(name = "version_label", length = 100)
    private String versionLabel;

    @Column(name = "source_url", nullable = false, length = 600)
    private String sourceUrl;

    @Column(name = "license_note", length = 300)
    private String licenseNote;

    @Column(name = "retrieved_on", nullable = false)
    private LocalDate retrievedOn;

    protected RecommendationEvidenceSource() {
    }

    public UUID getId() { return id; }
    public String getSourceCode() { return sourceCode; }
    public String getName() { return name; }
    public String getVersionLabel() { return versionLabel; }
    public String getSourceUrl() { return sourceUrl; }
    public String getLicenseNote() { return licenseNote; }
    public LocalDate getRetrievedOn() { return retrievedOn; }
}

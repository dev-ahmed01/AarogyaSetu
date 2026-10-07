package in.aarogya.nutrition.domain;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nutrition_sources")
public class NutritionSource {

    @Id
    private UUID id;

    @Column(name = "source_code", nullable = false, unique = true, length = 80)
    private String sourceCode;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(name = "version_label", length = 80)
    private String versionLabel;

    @Column(name = "source_type", nullable = false, length = 40)
    private String sourceType;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "license_label", length = 120)
    private String licenseLabel;

    @Column(name = "usage_note", length = 1000)
    private String usageNote;

    @Column(name = "retrieved_on")
    private LocalDate retrievedOn;

    protected NutritionSource() {
    }

    public UUID getId() { return id; }
    public String getSourceCode() { return sourceCode; }
    public String getName() { return name; }
    public String getVersionLabel() { return versionLabel; }
    public String getSourceType() { return sourceType; }
    public String getSourceUrl() { return sourceUrl; }
    public String getLicenseLabel() { return licenseLabel; }
    public String getUsageNote() { return usageNote; }
    public LocalDate getRetrievedOn() { return retrievedOn; }
}

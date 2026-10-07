package in.aarogya.regional.domain;

import java.util.UUID;

import in.aarogya.nutrition.domain.Food;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_region_affinities")
public class FoodRegionalAffinity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "region_code", nullable = false, length = 80)
    private String regionCode;

    @Column(name = "affinity_score", nullable = false)
    private int affinityScore;

    @Column(nullable = false, length = 40)
    private String relationship;

    @Column(nullable = false, length = 700)
    private String rationale;

    @Column(name = "source_code", nullable = false, length = 100)
    private String sourceCode;

    protected FoodRegionalAffinity() {
    }

    public Food getFood() { return food; }
    public String getRegionCode() { return regionCode; }
    public int getAffinityScore() { return affinityScore; }
    public String getRelationship() { return relationship; }
    public String getRationale() { return rationale; }
    public String getSourceCode() { return sourceCode; }
}

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
@Table(name = "regional_food_alternatives")
public class RegionalFoodAlternative {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_food_id", nullable = false)
    private Food sourceFood;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alternative_food_id", nullable = false)
    private Food alternativeFood;

    @Column(name = "region_code", nullable = false, length = 80)
    private String regionCode;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false, length = 700)
    private String rationale;

    @Column(nullable = false)
    private boolean active;

    protected RegionalFoodAlternative() {
    }

    public Food getSourceFood() { return sourceFood; }
    public Food getAlternativeFood() { return alternativeFood; }
    public String getRegionCode() { return regionCode; }
    public int getPriority() { return priority; }
    public String getRationale() { return rationale; }
    public boolean isActive() { return active; }
}

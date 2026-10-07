package in.aarogya.nutrition.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_portions")
public class FoodPortion {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal grams;

    @Column(name = "is_default", nullable = false)
    private boolean defaultPortion;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected FoodPortion() {
    }

    public UUID getId() { return id; }
    public String getLabel() { return label; }
    public BigDecimal getGrams() { return grams; }
    public boolean isDefaultPortion() { return defaultPortion; }
    public int getDisplayOrder() { return displayOrder; }
}

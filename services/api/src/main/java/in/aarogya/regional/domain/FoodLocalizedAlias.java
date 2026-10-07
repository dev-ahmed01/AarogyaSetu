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
@Table(name = "food_localized_aliases")
public class FoodLocalizedAlias {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "locale_code", nullable = false, length = 20)
    private String localeCode;

    @Column(nullable = false, length = 180)
    private String alias;

    protected FoodLocalizedAlias() {
    }

    public String getLocaleCode() { return localeCode; }
    public String getAlias() { return alias; }
}

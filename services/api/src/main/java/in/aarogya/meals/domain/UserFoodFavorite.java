package in.aarogya.meals.domain;

import java.time.Instant;
import java.util.UUID;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.nutrition.domain.Food;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "user_food_favorites",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_food_favorite",
        columnNames = {"user_id", "food_id"}
    )
)
public class UserFoodFavorite {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserFoodFavorite() {
    }

    public UserFoodFavorite(UserAccount user, Food food) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.food = food;
    }

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Food getFood() { return food; }
    public Instant getCreatedAt() { return createdAt; }
}

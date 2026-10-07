package in.aarogya.administration.domain;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "food_curation_reviews")
public class FoodCurationReview {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_user_id", nullable = false)
    private UserAccount reviewer;

    @Column(name = "reviewer_role", nullable = false, length = 30)
    private String reviewerRole;

    @Column(name = "review_action", nullable = false, length = 50)
    private String reviewAction;

    @Column(name = "from_status", nullable = false, length = 40)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 40)
    private String toStatus;

    @Column(name = "review_note", length = 1000)
    private String reviewNote;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected FoodCurationReview() {
    }

    public FoodCurationReview(
        Food food,
        UserAccount reviewer,
        String reviewerRole,
        String reviewAction,
        String fromStatus,
        String toStatus,
        String reviewNote
    ) {
        this.id = UUID.randomUUID();
        this.food = food;
        this.reviewer = reviewer;
        this.reviewerRole = reviewerRole;
        this.reviewAction = reviewAction;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reviewNote = reviewNote;
        this.occurredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Food getFood() { return food; }
    public UserAccount getReviewer() { return reviewer; }
    public String getReviewerRole() { return reviewerRole; }
    public String getReviewAction() { return reviewAction; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getReviewNote() { return reviewNote; }
    public Instant getOccurredAt() { return occurredAt; }
}

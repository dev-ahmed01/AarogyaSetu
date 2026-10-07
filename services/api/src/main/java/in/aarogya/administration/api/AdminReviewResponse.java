package in.aarogya.administration.api;

import java.time.Instant;
import java.util.UUID;

import in.aarogya.administration.domain.FoodCurationReview;

public record AdminReviewResponse(
    UUID id,
    UUID reviewerId,
    String reviewerName,
    String reviewerRole,
    String action,
    String fromStatus,
    String toStatus,
    String note,
    Instant occurredAt
) {
    public static AdminReviewResponse from(FoodCurationReview review) {
        return new AdminReviewResponse(
            review.getId(),
            review.getReviewer().getId(),
            review.getReviewer().getDisplayName(),
            review.getReviewerRole(),
            review.getReviewAction(),
            review.getFromStatus(),
            review.getToStatus(),
            review.getReviewNote(),
            review.getOccurredAt()
        );
    }
}

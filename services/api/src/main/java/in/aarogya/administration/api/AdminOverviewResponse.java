package in.aarogya.administration.api;

public record AdminOverviewResponse(
    String staffRole,
    long totalFoods,
    long needsReview,
    long inReview,
    long readyToPublish,
    long published,
    long unpublished,
    long sourceCount,
    Long userCount,
    Long mealEntryCount,
    Long healthRecordCount,
    Long auditEventCount
) {
}

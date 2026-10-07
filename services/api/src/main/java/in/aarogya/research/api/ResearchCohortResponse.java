package in.aarogya.research.api;

public record ResearchCohortResponse(
    String segmentType,
    String segmentValue,
    Integer participantCount,
    boolean suppressed
) {
}

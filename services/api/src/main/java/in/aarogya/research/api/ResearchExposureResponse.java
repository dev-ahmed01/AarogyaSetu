package in.aarogya.research.api;

public record ResearchExposureResponse(
    String eventCode,
    Integer participantCount,
    boolean suppressed
) {
}

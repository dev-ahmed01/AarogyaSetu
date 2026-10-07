package in.aarogya.recommendations.api;

import in.aarogya.recommendations.domain.RecommendationEvidenceSource;

public record EvidenceSourceResponse(
    String code,
    String name,
    String version,
    String url
) {

    public static EvidenceSourceResponse from(RecommendationEvidenceSource source) {
        if (source == null) {
            return null;
        }

        return new EvidenceSourceResponse(
            source.getSourceCode(),
            source.getName(),
            source.getVersionLabel(),
            source.getSourceUrl()
        );
    }
}

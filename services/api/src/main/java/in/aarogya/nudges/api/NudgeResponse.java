package in.aarogya.nudges.api;

import java.time.Instant;
import java.util.UUID;

import in.aarogya.nudges.domain.NudgeInstance;

public record NudgeResponse(
    UUID id,
    String ruleCode,
    int ruleVersion,
    String category,
    String severity,
    String status,
    String title,
    String message,
    String actionLabel,
    String actionHref,
    String reasonCode,
    String sourceType,
    String sourceRef,
    String evidenceLabel,
    String evidenceUrl,
    Instant firstGeneratedAt,
    Instant lastEvaluatedAt,
    Instant snoozedUntil,
    Instant acknowledgedAt,
    Instant dismissedAt,
    Instant resolvedAt
) {

    public static NudgeResponse from(NudgeInstance nudge) {
        return new NudgeResponse(
            nudge.getId(),
            nudge.getRuleCode(),
            nudge.getRuleVersion(),
            nudge.getCategory(),
            nudge.getSeverity(),
            nudge.getStatus(),
            nudge.getTitle(),
            nudge.getMessage(),
            nudge.getActionLabel(),
            nudge.getActionHref(),
            nudge.getReasonCode(),
            nudge.getSourceType(),
            nudge.getSourceRef(),
            nudge.getEvidenceLabel(),
            nudge.getEvidenceUrl(),
            nudge.getFirstGeneratedAt(),
            nudge.getLastEvaluatedAt(),
            nudge.getSnoozedUntil(),
            nudge.getAcknowledgedAt(),
            nudge.getDismissedAt(),
            nudge.getResolvedAt()
        );
    }
}

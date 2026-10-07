package in.aarogya.administration.api;

import java.time.Instant;
import java.util.UUID;

import in.aarogya.security.domain.SecurityAuditEvent;

public record AuditEventResponse(
    Long id,
    UUID actorId,
    String actorName,
    String actorRole,
    String eventType,
    String outcome,
    String subject,
    String metadata,
    Instant occurredAt
) {
    public static AuditEventResponse from(SecurityAuditEvent event) {
        var actor = event.getUser();

        return new AuditEventResponse(
            event.getId(),
            actor == null ? null : actor.getId(),
            actor == null ? "System" : actor.getDisplayName(),
            actor == null ? "SYSTEM" : actor.getRole().name(),
            event.getEventType(),
            event.getEventOutcome(),
            event.getSubject(),
            event.getMetadataJson(),
            event.getOccurredAt()
        );
    }
}

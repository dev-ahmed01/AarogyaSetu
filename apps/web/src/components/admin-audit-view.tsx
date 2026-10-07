"use client";

import { StatusChip } from "@/components/ui";
import type { AuditEvent } from "@/lib/admin";

export function AdminAuditView({
  events
}: {
  events: AuditEvent[];
}) {
  return (
    <section className="adminAuditSection">
      <div className="adminSectionHeading">
        <div>
          <span className="cardEyebrow">
            Operational audit
          </span>
          <h2>Recent security and content events</h2>
        </div>
        <span>{events.length + " recent events"}</span>
      </div>

      <div className="adminAuditList">
        {events.length === 0 ? (
          <div className="adminQueueEmpty">
            No audit events were returned.
          </div>
        ) : (
          events.map((event) => (
            <article key={event.id}>
              <div>
                <strong>{pretty(event.eventType)}</strong>
                <span>
                  {event.actorName
                    + " · "
                    + pretty(event.actorRole)}
                </span>
              </div>

              <div>
                <StatusChip
                  tone={
                    event.outcome === "SUCCESS"
                      ? "positive"
                      : "warm"
                  }
                >
                  {event.outcome}
                </StatusChip>
                <time>
                  {formatDateTime(event.occurredAt)}
                </time>
              </div>

              <p>
                {(event.subject ?? "No subject")
                  + (event.metadata
                    ? " · " + event.metadata
                    : "")}
              </p>
            </article>
          ))
        )}
      </div>
    </section>
  );
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) =>
      letter.toUpperCase()
    );
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import { trackResearchEvent } from "@/lib/research";
import {
  acknowledgeNudge,
  announceNudgeChange,
  dismissNudge,
  evaluateNudges,
  getNudges,
  getNudgeSummary,
  snoozeNudge,
  type Nudge,
  type NudgeSummary
} from "@/lib/nudges";

export function AlertsClient() {
  const [items, setItems] = useState<Nudge[]>([]);
  const [summary, setSummary] = useState<NudgeSummary>({
    activeCount: 0,
    attentionCount: 0,
    snoozedCount: 0
  });
  const [history, setHistory] = useState(false);
  const [loading, setLoading] = useState(true);
  const [workingId, setWorkingId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function refresh(includeHistory = history) {
    setLoading(true);
    setError(null);

    try {
      await evaluateNudges(todayKey());
      const [nextItems, nextSummary] = await Promise.all([
        getNudges(includeHistory),
        getNudgeSummary()
      ]);
      setItems(nextItems);
      setSummary(nextSummary);
      announceNudgeChange();
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not load alerts."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void trackResearchEvent("ALERTS_VIEWED");
    void refresh(false);
  }, []);

  async function mutate(
    id: string,
    action: () => Promise<Nudge>
  ) {
    setWorkingId(id);
    setError(null);

    try {
      await action();
      const [nextItems, nextSummary] = await Promise.all([
        getNudges(history),
        getNudgeSummary()
      ]);
      setItems(nextItems);
      setSummary(nextSummary);
      announceNudgeChange();
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not update this alert."
      );
    } finally {
      setWorkingId(null);
    }
  }

  async function toggleHistory() {
    const next = !history;
    setHistory(next);
    setLoading(true);

    try {
      setItems(await getNudges(next));
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not load alert history."
      );
    } finally {
      setLoading(false);
    }
  }

  const activeItems = useMemo(
    () => items.filter((item) => item.status === "ACTIVE"),
    [items]
  );

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Alerts"
        title="What needs attention, without the noise."
        description="Aarogya materializes explainable guidance into a small alert inbox with cooldowns, snooze and dismissal. It does not infer diagnoses from health-record values."
        action={
          <button
            className="button button--secondary"
            type="button"
            onClick={() => void toggleHistory()}
          >
            {history ? "Hide history" : "Show history"}
          </button>
        }
      />

      {error ? (
        <div className="formNotice formNotice--error" role="alert">
          {error}
        </div>
      ) : null}

      <section className="alertSummaryStrip">
        <div>
          <span>Active</span>
          <strong>{summary.activeCount}</strong>
        </div>
        <div>
          <span>Needs attention</span>
          <strong>{summary.attentionCount}</strong>
        </div>
        <div>
          <span>Snoozed</span>
          <strong>{summary.snoozedCount}</strong>
        </div>
        <div>
          <span>Evaluation</span>
          <strong>On demand</strong>
        </div>
      </section>

      {loading ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">A</div>
          <p>Checking current signals…</p>
        </div>
      ) : items.length === 0 ? (
        <section className="alertEmpty">
          <span className="cardEyebrow">All clear</span>
          <h2>No current nudges need your attention.</h2>
          <p>
            Aarogya only surfaces a nudge when an explainable rule or consented data-consistency signal is active.
          </p>
        </section>
      ) : (
        <div className="alertList">
          {items.map((item) => (
            <AlertCard
              key={item.id}
              item={item}
              working={workingId === item.id}
              onSnooze={() =>
                mutate(item.id, () => snoozeNudge(item.id, 24))
              }
              onAcknowledge={() =>
                mutate(item.id, () => acknowledgeNudge(item.id))
              }
              onDismiss={() =>
                mutate(item.id, () => dismissNudge(item.id))
              }
            />
          ))}
        </div>
      )}

      {!history && activeItems.length === 0 && summary.snoozedCount > 0 ? (
        <p className="alertFootnote">
          Current nudges are snoozed. Use “Show history” if you want to review their state.
        </p>
      ) : null}

      <section className="alertBoundaryNote">
        <span className="cardEyebrow">Clinical boundary</span>
        <h2>Health records can constrain or contextualize prompts, not become diagnoses.</h2>
        <p>
          Phase 10 does not classify lab values as normal/abnormal, does not prescribe treatment and does not let synthetic ABDM demo records drive personalization.
        </p>
      </section>
    </main>
  );
}

function AlertCard({
  item,
  working,
  onSnooze,
  onAcknowledge,
  onDismiss
}: {
  item: Nudge;
  working: boolean;
  onSnooze: () => void;
  onAcknowledge: () => void;
  onDismiss: () => void;
}) {
  const tone =
    item.severity === "ATTENTION"
      ? "attention"
      : item.severity === "INFO"
        ? "neutral"
        : "warm";

  return (
    <article className={`alertCard alertCard--${item.severity.toLowerCase()}`}>
      <div className="alertCard__top">
        <div>
          <div className="alertCard__chips">
            <StatusChip tone={tone}>{pretty(item.severity)}</StatusChip>
            <span>{pretty(item.category)}</span>
            <span>{pretty(item.status)}</span>
          </div>
          <h2>{item.title}</h2>
        </div>
        <span className="alertCard__time">
          {formatRelative(item.lastEvaluatedAt)}
        </span>
      </div>

      <p className="alertCard__message">{item.message}</p>

      <div className="alertCard__meta">
        <div>
          <span>Reason</span>
          <strong>{pretty(item.reasonCode)}</strong>
        </div>
        <div>
          <span>Rule</span>
          <strong>{item.ruleCode} · v{item.ruleVersion}</strong>
        </div>
        <div>
          <span>Source</span>
          <strong>{pretty(item.sourceType)}</strong>
        </div>
      </div>

      {item.evidenceLabel ? (
        <div className="alertCard__evidence">
          <span>Evidence / provenance</span>
          {item.evidenceUrl ? (
            <a href={item.evidenceUrl} target="_blank" rel="noreferrer">
              {item.evidenceLabel}
            </a>
          ) : (
            <strong>{item.evidenceLabel}</strong>
          )}
        </div>
      ) : null}

      {item.status === "SNOOZED" && item.snoozedUntil ? (
        <p className="alertCard__snoozed">
          Snoozed until {formatDateTime(item.snoozedUntil)}.
        </p>
      ) : null}

      {item.status === "ACTIVE" || item.status === "SNOOZED" ? (
        <div className="alertCard__actions">
          {item.actionHref && item.actionLabel ? (
            <Link className="button button--primary" href={item.actionHref}>
              {item.actionLabel}
            </Link>
          ) : null}

          {item.status === "ACTIVE" ? (
            <button
              className="button button--secondary"
              type="button"
              disabled={working}
              onClick={onSnooze}
            >
              Snooze 24h
            </button>
          ) : null}

          <button
            className="button button--ghost"
            type="button"
            disabled={working}
            onClick={onAcknowledge}
          >
            Acknowledge
          </button>

          <button
            className="textAction"
            type="button"
            disabled={working}
            onClick={onDismiss}
          >
            Dismiss
          </button>
        </div>
      ) : (
        <div className="alertCard__historyState">
          {pretty(item.status)}
        </div>
      )}
    </article>
  );
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}

function formatRelative(value: string) {
  const minutes = Math.max(
    0,
    Math.round((Date.now() - new Date(value).getTime()) / 60000)
  );

  if (minutes < 1) return "Just now";
  if (minutes < 60) return `${minutes}m ago`;

  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours}h ago`;

  return formatDateTime(value);
}

function todayKey() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

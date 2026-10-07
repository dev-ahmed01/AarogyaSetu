"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import { trackResearchEvent } from "@/lib/research";
import {
  getTodayRecommendations,
  type RecommendationAssessment,
  type RecommendationItem
} from "@/lib/recommendations";

export function GuidanceClient() {
  const [assessment, setAssessment] =
    useState<RecommendationAssessment | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void trackResearchEvent("GUIDANCE_VIEWED");
    getTodayRecommendations(todayKey())
      .then(setAssessment)
      .catch((cause) =>
        setError(cause instanceof Error ? cause.message : "Could not load guidance.")
      );
  }, []);

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Guidance"
        title="Recommendations you can inspect, not just accept."
        description="Aarogya shows the observation, rule, reference and safety boundary behind every recommendation. This remains wellness guidance, not diagnosis or treatment."
        action={
          <Link className="button button--secondary" href="/meals">
            Review meal log
          </Link>
        }
      />

      {error ? (
        <div className="formNotice formNotice--error" role="alert">
          {error}
        </div>
      ) : null}

      {!assessment ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">A</div>
          <p>Evaluating your logged data…</p>
        </div>
      ) : (
        <>
          <section className="guidanceSummary">
            <div>
              <span className="cardEyebrow">Engine status</span>
              <StatusChip tone={statusTone(assessment.status)}>
                {statusLabel(assessment.status)}
              </StatusChip>
            </div>

            <div>
              <span>Trend window</span>
              <strong>
                {formatDate(assessment.analysisFrom)} – {formatDate(assessment.analysisTo)}
              </strong>
            </div>

            <div>
              <span>Observed days</span>
              <strong>
                {assessment.observedDays} / {assessment.windowDays}
              </strong>
            </div>

            <div>
              <span>Trend minimum</span>
              <strong>{assessment.minimumTrendDays} days</strong>
            </div>
          </section>

          {assessment.recommendations.length > 0 ? (
            <section className="guidanceList" aria-label="Personalized guidance">
              {assessment.recommendations.map((item) => (
                <GuidanceCard
                  key={`${item.ruleCode}-${item.ruleVersion}-${item.observation}`}
                  item={item}
                />
              ))}
            </section>
          ) : (
            <section className="guidanceEmpty">
              <span className="cardEyebrow">No recommendation forced</span>
              <h2>{emptyTitle(assessment.status)}</h2>
              <p>{emptyCopy(assessment)}</p>
              <Link
                className="button button--primary"
                href={
                  assessment.status === "PERSONALIZATION_PAUSED"
                    || assessment.status === "PROFILE_INCOMPLETE"
                    ? "/profile"
                    : "/meals"
                }
              >
                {assessment.status === "PERSONALIZATION_PAUSED"
                  || assessment.status === "PROFILE_INCOMPLETE"
                  ? "Review profile"
                  : "Continue meal logging"}
              </Link>
            </section>
          )}

          {assessment.notices.length > 0 ? (
            <section className="guidanceNotices">
              <span className="cardEyebrow">Safety & interpretation</span>
              <div>
                {assessment.notices.map((notice) => (
                  <p key={notice}>{notice}</p>
                ))}
              </div>
            </section>
          ) : null}
        </>
      )}
    </main>
  );
}

function GuidanceCard({ item }: { item: RecommendationItem }) {
  const safety = item.safetyClass === "SAFETY_ATTENTION";

  return (
    <article className={safety ? "guidanceCard guidanceCard--safety" : "guidanceCard"}>
      <div className="guidanceCard__top">
        <div>
          <span className="cardEyebrow">
            {safety ? "Safety attention" : "Explainable guidance"}
          </span>
          <h2>{item.title}</h2>
        </div>
        <StatusChip tone={safety ? "attention" : item.priority === 1 ? "warm" : "positive"}>
          {item.reasonCode.replaceAll("_", " ").toLowerCase()}
        </StatusChip>
      </div>

      <div className="guidanceCard__sections">
        <section>
          <span>What Aarogya noticed</span>
          <p>{item.observation}</p>
        </section>
        <section>
          <span>Why it matters</span>
          <p>{item.whyItMatters}</p>
        </section>
        <section>
          <span>What you could consider</span>
          <p>{item.consideration}</p>
        </section>
      </div>

      <footer className="guidanceCard__footer">
        <div>
          <span>Rule</span>
          <strong>{item.ruleCode} · v{item.ruleVersion}</strong>
        </div>

        {item.observedValue !== null && item.referenceValue !== null ? (
          <div>
            <span>Observed / reference</span>
            <strong>
              {formatNumber(item.observedValue)} / {formatNumber(item.referenceValue)}{" "}
              {item.unit}
            </strong>
          </div>
        ) : null}

        {item.evidence ? (
          <a href={item.evidence.url} target="_blank" rel="noreferrer">
            <span>Evidence</span>
            <strong>{item.evidence.name}</strong>
          </a>
        ) : null}
      </footer>
    </article>
  );
}

function emptyTitle(status: string) {
  if (status === "PERSONALIZATION_PAUSED") {
    return "Personalization is paused.";
  }

  if (status === "PROFILE_INCOMPLETE") {
    return "Finish your profile before Aarogya interprets it.";
  }

  if (status === "NEED_MORE_DATA") {
    return "Aarogya needs a little more real meal history.";
  }

  return "Nothing needs to be pushed at you today.";
}

function emptyCopy(assessment: RecommendationAssessment) {
  if (assessment.status === "NEED_MORE_DATA") {
    return `Trend rules need at least ${assessment.minimumTrendDays} sufficiently logged completed days. Safety conflicts can still appear immediately when relevant.`;
  }

  return assessment.notices[0] ?? "No explainable recommendation was triggered.";
}

function statusLabel(status: string) {
  return status
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function statusTone(status: string): "positive" | "warm" | "attention" | undefined {
  if (status.includes("SAFETY")) return "attention";
  if (status === "NEED_MORE_DATA" || status.includes("LIMIT")) return "warm";
  if (status === "READY") return "positive";
  return undefined;
}

function formatNumber(value: number) {
  return new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 1
  }).format(value);
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short"
  }).format(new Date(`${value}T12:00:00`));
}

function todayKey() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

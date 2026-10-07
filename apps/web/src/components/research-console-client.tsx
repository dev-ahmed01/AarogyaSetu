"use client";

import { useEffect, useMemo, useState } from "react";

import { useAuth } from "@/components/auth-gate";
import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import {
  downloadResearchExport,
  getResearchMetricDefinitions,
  getResearchOverview,
  type ResearchMetricDefinition,
  type ResearchOverview
} from "@/lib/research";

export function ResearchConsoleClient() {
  const { user } = useAuth();
  const admin = user.role === "ADMIN";

  const [from, setFrom] = useState(
    dateKey(daysAgo(29))
  );
  const [to, setTo] = useState(dateKey(new Date()));
  const [overview, setOverview] =
    useState<ResearchOverview | null>(null);
  const [definitions, setDefinitions] =
    useState<ResearchMetricDefinition[]>([]);
  const [loading, setLoading] = useState(admin);
  const [exporting, setExporting] = useState(false);
  const [error, setError] =
    useState<string | null>(null);

  useEffect(() => {
    if (!admin) {
      setLoading(false);
      return;
    }

    void load();
  }, [admin]);

  async function load() {
    setLoading(true);
    setError(null);

    try {
      const [nextOverview, nextDefinitions] =
        await Promise.all([
          getResearchOverview(from, to),
          getResearchMetricDefinitions()
        ]);

      setOverview(nextOverview);
      setDefinitions(nextDefinitions);
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setLoading(false);
    }
  }

  async function exportCsv() {
    setExporting(true);
    setError(null);

    try {
      const csv = await downloadResearchExport(
        from,
        to
      );
      const blob = new Blob([csv], {
        type: "text/csv;charset=utf-8"
      });
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download =
        "aarogya-research-evaluation.csv";
      anchor.click();
      URL.revokeObjectURL(url);
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setExporting(false);
    }
  }

  const latestDefinitions = useMemo(() => {
    const map = new Map<
      string,
      ResearchMetricDefinition
    >();

    for (const item of definitions) {
      const current = map.get(item.metricCode);
      if (
        !current
        || item.metricVersion > current.metricVersion
      ) {
        map.set(item.metricCode, item);
      }
    }

    return Array.from(map.values()).sort((a, b) =>
      a.name.localeCompare(b.name)
    );
  }, [definitions]);

  if (!admin) {
    return (
      <main className="workspacePage">
        <PageHeader
          eyebrow="Research evaluation"
          title="This workspace is admin-only."
          description="Research outputs are aggregate-only and remain separate from nutritionist content operations."
        />
        <section className="researchBoundaryPanel">
          <span className="cardEyebrow">
            Access boundary
          </span>
          <h2>
            Individual participant data is not exposed here.
          </h2>
          <p>
            The server requires the ADMIN role for every
            evaluation endpoint and export.
          </p>
        </section>
      </main>
    );
  }

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Research evaluation"
        title="Evaluate behaviour without identifying participants."
        description="Aarogya reports only consented, de-identified aggregate signals. Small cohorts are suppressed and pre/post results are descriptive associations, not causal claims."
        action={
          <button
            className="button button--secondary"
            type="button"
            disabled={exporting || !overview}
            onClick={() => void exportCsv()}
          >
            {exporting
              ? "Preparing export…"
              : "Export aggregate CSV"}
          </button>
        }
      />

      <section className="researchWindowBar">
        <label>
          <span>From</span>
          <input
            type="date"
            value={from}
            max={to}
            onChange={(event) =>
              setFrom(event.target.value)
            }
          />
        </label>
        <label>
          <span>To</span>
          <input
            type="date"
            value={to}
            min={from}
            max={dateKey(new Date())}
            onChange={(event) =>
              setTo(event.target.value)
            }
          />
        </label>
        <button
          className="button button--primary"
          type="button"
          onClick={() => void load()}
        >
          Recalculate
        </button>
      </section>

      {error ? (
        <div
          className="formNotice formNotice--error"
          role="alert"
        >
          {error}
        </div>
      ) : null}

      {loading || !overview ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">
            A
          </div>
          <p>Calculating privacy-safe metrics…</p>
        </div>
      ) : (
        <>
          <section className="researchSummaryStrip">
            <Summary
              label="Opted-in participants"
              value={String(
                overview.optedInParticipants
              )}
            />
            <Summary
              label="Minimum cohort"
              value={String(
                overview.minimumCohortSize
              )}
            />
            <Summary
              label="Window"
              value={
                daysBetween(
                  overview.from,
                  overview.to
                ) + " days"
              }
            />
            <Summary
              label="Export"
              value="Aggregate only"
            />
          </section>

          <section className="researchMetricGrid">
            {overview.metrics.map((metric) => (
              <article
                className="researchMetricCard"
                key={metric.metricCode}
              >
                <div>
                  <span className="cardEyebrow">
                    {pretty(metric.metricCode)}
                  </span>
                  <StatusChip
                    tone={
                      metric.suppressed
                        ? "warm"
                        : "positive"
                    }
                  >
                    {metric.suppressed
                      ? "Suppressed"
                      : "Reportable"}
                  </StatusChip>
                </div>
                <h2>
                  {metric.suppressed
                    ? "—"
                    : formatMetric(
                        metric.value,
                        metric.unit
                      )}
                </h2>
                <p>{metric.interpretation}</p>
                <footer>
                  {metric.eligibleParticipants
                    + " eligible participants"}
                </footer>
              </article>
            ))}
          </section>

          <div className="researchTwoColumn">
            <section className="researchPanel">
              <div className="researchSectionHeading">
                <div>
                  <span className="cardEyebrow">
                    Feature exposure
                  </span>
                  <h2>
                    Distinct participants by feature
                  </h2>
                </div>
              </div>

              <div className="researchExposureList">
                {overview.featureExposures.map(
                  (item) => (
                    <div key={item.eventCode}>
                      <span>
                        {pretty(item.eventCode)}
                      </span>
                      <strong>
                        {item.suppressed
                          ? "Suppressed"
                          : item.participantCount}
                      </strong>
                    </div>
                  )
                )}
              </div>
            </section>

            <section className="researchPanel">
              <div className="researchSectionHeading">
                <div>
                  <span className="cardEyebrow">
                    Dietary-pattern cohorts
                  </span>
                  <h2>
                    Broad profile distribution
                  </h2>
                </div>
              </div>

              <div className="researchExposureList">
                {overview.dietaryPatternCohorts
                  .length === 0 ? (
                  <p className="researchEmptyCopy">
                    No eligible profile cohorts yet.
                  </p>
                ) : (
                  overview.dietaryPatternCohorts.map(
                    (item) => (
                      <div
                        key={
                          item.segmentType
                          + ":"
                          + item.segmentValue
                        }
                      >
                        <span>
                          {pretty(
                            item.segmentValue
                          )}
                        </span>
                        <strong>
                          {item.suppressed
                            ? "Suppressed"
                            : item.participantCount}
                        </strong>
                      </div>
                    )
                  )
                )}
              </div>
            </section>
          </div>

          <section className="researchDefinitionsPanel">
            <div className="researchSectionHeading">
              <div>
                <span className="cardEyebrow">
                  Reproducibility
                </span>
                <h2>Versioned metric definitions</h2>
              </div>
              <span>
                {latestDefinitions.length
                  + " definitions"}
              </span>
            </div>

            <div className="researchDefinitionList">
              {latestDefinitions.map(
                (definition) => (
                  <article
                    key={
                      definition.metricCode
                      + ":"
                      + definition.metricVersion
                    }
                  >
                    <div>
                      <strong>
                        {definition.name}
                      </strong>
                      <span>
                        {definition.metricCode
                          + " · v"
                          + definition.metricVersion}
                      </span>
                    </div>
                    <p>
                      {definition.description}
                    </p>
                    <small>
                      {"Unit: "
                        + definition.unit
                        + " · minimum cohort "
                        + definition.minimumCohortSize}
                    </small>
                  </article>
                )
              )}
            </div>
          </section>

          <section className="researchBoundaryPanel">
            <div>
              <span className="cardEyebrow">
                Interpretation boundary
              </span>
              <h2>
                Association is not evidence of causation.
              </h2>
            </div>
            <div>
              {overview.notices.map((notice) => (
                <p key={notice}>{notice}</p>
              ))}
            </div>
          </section>
        </>
      )}
    </main>
  );
}

function Summary({
  label,
  value
}: {
  label: string;
  value: string;
}) {
  return (
    <div>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function formatMetric(
  value: number | null,
  unit: string
) {
  if (value === null) return "—";

  return new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 2
  }).format(value)
    + " "
    + unit;
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) =>
      letter.toUpperCase()
    );
}

function dateKey(date: Date) {
  const year = date.getFullYear();
  const month = String(
    date.getMonth() + 1
  ).padStart(2, "0");
  const day = String(
    date.getDate()
  ).padStart(2, "0");

  return year + "-" + month + "-" + day;
}

function daysAgo(days: number) {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date;
}

function daysBetween(
  from: string,
  to: string
) {
  const start = new Date(from + "T00:00:00");
  const end = new Date(to + "T00:00:00");

  return Math.round(
    (end.getTime() - start.getTime())
      / 86400000
  ) + 1;
}

function messageOf(cause: unknown) {
  return cause instanceof Error
    ? cause.message
    : "Research evaluation could not be loaded.";
}

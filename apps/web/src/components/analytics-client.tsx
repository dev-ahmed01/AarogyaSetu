"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import {
  getLongitudinalAnalytics,
  type DailyNutritionPoint,
  type LongitudinalAnalytics,
  type NutrientTrend
} from "@/lib/analytics";

type WindowDays = 7 | 30;
type MetricKey = "energyKcal" | "proteinG" | "fibreG";

const METRICS: {
  key: MetricKey;
  label: string;
  unit: string;
}[] = [
  { key: "energyKcal", label: "Energy", unit: "kcal" },
  { key: "proteinG", label: "Protein", unit: "g" },
  { key: "fibreG", label: "Fibre", unit: "g" }
];

export function AnalyticsClient() {
  const [windowDays, setWindowDays] = useState<WindowDays>(7);
  const [metric, setMetric] = useState<MetricKey>("proteinG");
  const [analytics, setAnalytics] = useState<LongitudinalAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);

    getLongitudinalAnalytics(todayKey(), windowDays)
      .then(setAnalytics)
      .catch((cause) =>
        setError(
          cause instanceof Error
            ? cause.message
            : "Could not load longitudinal analytics."
        )
      )
      .finally(() => setLoading(false));
  }, [windowDays]);

  const selectedMetric = useMemo(
    () => METRICS.find((item) => item.key === metric) ?? METRICS[1],
    [metric]
  );

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Longitudinal analytics"
        title="See patterns without inventing certainty."
        description="Aarogya compares recorded windows, shows exactly how complete the data is, and keeps missing days separate from zero intake."
        action={
          <div className="analyticsWindowControl" aria-label="Analytics window">
            <button
              className={windowDays === 7 ? "is-active" : ""}
              type="button"
              onClick={() => setWindowDays(7)}
            >
              7 days
            </button>
            <button
              className={windowDays === 30 ? "is-active" : ""}
              type="button"
              onClick={() => setWindowDays(30)}
            >
              30 days
            </button>
          </div>
        }
      />

      {error ? (
        <div className="formNotice formNotice--error" role="alert">
          {error}
        </div>
      ) : null}

      {loading || !analytics ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">A</div>
          <p>Building the comparison window…</p>
        </div>
      ) : (
        <>
          <section className="analyticsSummaryStrip">
            <Summary
              label="Logged days"
              value={`${analytics.coverage.loggedDays}/${analytics.windowDays}`}
            />
            <Summary
              label="Coverage"
              value={`${analytics.coverage.coveragePercent}%`}
            />
            <Summary
              label="Food entries"
              value={String(analytics.currentEntryCount)}
            />
            <Summary
              label="Previous window"
              value={`${analytics.previousCoverage.loggedDays}/${analytics.windowDays} days`}
            />
          </section>

          <section className="analyticsCoverageNote">
            <div>
              <StatusChip tone={coverageTone(analytics.coverage.status)}>
                {analytics.coverage.label}
              </StatusChip>
              <strong>
                {formatRange(analytics.currentFrom, analytics.currentTo)}
              </strong>
            </div>
            <p>
              Comparisons use logged-day averages. A day without a meal log is
              missing evidence, not zero nutrition.
            </p>
          </section>

          <section className="analyticsTrendPanel">
            <div className="analyticsSectionHeading">
              <div>
                <span className="cardEyebrow">Nutrition timeline</span>
                <h2>Recorded daily totals</h2>
              </div>

              <div className="analyticsMetricTabs" aria-label="Nutrient metric">
                {METRICS.map((item) => (
                  <button
                    className={metric === item.key ? "is-active" : ""}
                    key={item.key}
                    type="button"
                    onClick={() => setMetric(item.key)}
                  >
                    {item.label}
                  </button>
                ))}
              </div>
            </div>

            <NutritionBars
              points={analytics.dailyNutrition}
              metric={selectedMetric}
              windowDays={windowDays}
            />

            <div className="nutrientComparisonGrid">
              {analytics.nutrientTrends.map((trend) => (
                <NutrientComparison key={trend.nutrientCode} trend={trend} />
              ))}
            </div>
          </section>

          <div className="analyticsTwoColumn">
            <section className="analyticsPatternPanel">
              <span className="cardEyebrow">Meal pattern</span>
              <h2>What was actually recorded</h2>
              <div className="mealPatternList">
                {analytics.mealPatterns.map((pattern) => (
                  <div key={pattern.mealType}>
                    <div>
                      <strong>{pretty(pattern.mealType)}</strong>
                      <span>
                        {pattern.entryCount} {pattern.entryCount === 1 ? "entry" : "entries"}
                      </span>
                    </div>
                    <b>{pattern.daysPresent} days</b>
                  </div>
                ))}
              </div>
            </section>

            <section className="analyticsInsightPanel">
              <span className="cardEyebrow">Descriptive insights</span>
              <h2>What changed in the recorded data</h2>
              <div className="analyticsInsightList">
                {analytics.insights.map((insight, index) => (
                  <p key={`${index}:${insight}`}>
                    <span>{index + 1}</span>
                    {insight}
                  </p>
                ))}
              </div>
            </section>
          </div>

          <section className="healthTrendPanel">
            <div className="analyticsSectionHeading">
              <div>
                <span className="cardEyebrow">Health-record series</span>
                <h2>{analytics.healthTrend.label}</h2>
              </div>
              <StatusChip
                tone={
                  analytics.healthTrend.status === "AVAILABLE"
                    ? "positive"
                    : analytics.healthTrend.status === "CONSENT_REQUIRED"
                      ? "warm"
                      : "neutral"
                }
              >
                {pretty(analytics.healthTrend.status)}
              </StatusChip>
            </div>

            <p className="healthTrendPanel__notice">
              {analytics.healthTrend.notice}
            </p>

            {analytics.healthTrend.points.length > 0 ? (
              <div className="healthTrendTimeline">
                {analytics.healthTrend.points.map((point) => (
                  <div key={point.observedAt}>
                    <span>{formatDate(point.observedAt)}</span>
                    <strong>
                      {formatNumber(point.value)} {point.unit}
                    </strong>
                    <small>Manual self-report</small>
                  </div>
                ))}
              </div>
            ) : (
              <div className="analyticsEmptyInline">
                <span>No eligible health-series points are being analyzed.</span>
                <Link className="quietLink" href="/health">
                  Review health records
                </Link>
              </div>
            )}
          </section>

          <section className="analyticsQualityPanel">
            <div>
              <span className="cardEyebrow">Data-quality boundary</span>
              <h2>The chart is only as complete as the record.</h2>
            </div>
            <div>
              {analytics.dataQualityNotices.map((notice) => (
                <p key={notice}>{notice}</p>
              ))}
            </div>
          </section>
        </>
      )}
    </main>
  );
}

function NutritionBars({
  points,
  metric,
  windowDays
}: {
  points: DailyNutritionPoint[];
  metric: { key: MetricKey; label: string; unit: string };
  windowDays: WindowDays;
}) {
  const values = points
    .map((point) => point[metric.key])
    .filter((value): value is number => value !== null);
  const max = Math.max(...values, 1);

  return (
    <div className="analyticsBarsWrap">
      <div
        className={windowDays === 30 ? "analyticsBars analyticsBars--30" : "analyticsBars"}
        role="img"
        aria-label={`${metric.label} totals across the current ${windowDays}-day window`}
      >
        {points.map((point, index) => {
          const value = point[metric.key];
          const height = value === null ? 0 : Math.max(4, (value / max) * 100);
          const showLabel =
            windowDays === 7 || index === 0 || index === points.length - 1 || index % 5 === 0;

          return (
            <div
              className={point.logged ? "analyticsBarDay is-logged" : "analyticsBarDay"}
              key={point.date}
            >
              <div className="analyticsBarDay__value">
                {value !== null ? (
                  <span
                    style={{ height: `${height}%` }}
                    title={`${formatNumber(value)} ${metric.unit}`}
                  />
                ) : (
                  <i title="No meal log for this day" />
                )}
              </div>
              <small>{showLabel ? shortDate(point.date) : ""}</small>
            </div>
          );
        })}
      </div>
      <div className="analyticsBarsLegend">
        <span>
          <b /> Logged value
        </span>
        <span>
          <i /> Missing day
        </span>
      </div>
    </div>
  );
}

function NutrientComparison({ trend }: { trend: NutrientTrend }) {
  return (
    <article className="nutrientComparisonCard">
      <div>
        <span>{trend.label}</span>
        <StatusChip
          tone={
            trend.direction === "INSUFFICIENT_DATA"
              ? "neutral"
              : trend.direction === "SIMILAR"
                ? "positive"
                : "warm"
          }
        >
          {pretty(trend.direction)}
        </StatusChip>
      </div>
      <strong>
        {trend.currentLoggedDayAverage === null
          ? "—"
          : `${formatNumber(trend.currentLoggedDayAverage)} ${trend.unit}`}
      </strong>
      <small>Current logged-day average</small>
      <p>{trend.interpretation}</p>
      <footer>
        {trend.currentObservedDays} current · {trend.previousObservedDays} previous observed days
      </footer>
    </article>
  );
}

function Summary({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function coverageTone(
  status: LongitudinalAnalytics["coverage"]["status"]
): "neutral" | "positive" | "warm" {
  if (status === "USABLE") return "positive";
  if (status === "EMPTY") return "neutral";
  return "warm";
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function shortDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short"
  }).format(new Date(`${value}T00:00:00`));
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric"
  }).format(new Date(value));
}

function formatRange(from: string, to: string) {
  return `${shortDate(from)} – ${shortDate(to)}`;
}

function formatNumber(value: number) {
  return new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 1
  }).format(value);
}

function todayKey() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

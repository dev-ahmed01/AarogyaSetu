"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { PageHeader } from "@/components/page-header";
import { ProgressBar, StatusChip } from "@/components/ui";
import {
  evaluateProgress,
  pauseMealLoggingGoal,
  resumeMealLoggingGoal,
  updateMealLoggingGoal,
  type ProgressOverview
} from "@/lib/progress";

export function ProgressClient() {
  const [overview, setOverview] = useState<ProgressOverview | null>(null);
  const [target, setTarget] = useState(4);
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    evaluateProgress(todayKey())
      .then((result) => {
        setOverview(result);
        if (result.mealLoggingGoal) {
          setTarget(result.mealLoggingGoal.targetValue);
        }
      })
      .catch((cause) =>
        setError(
          cause instanceof Error
            ? cause.message
            : "Could not load progress."
        )
      )
      .finally(() => setLoading(false));
  }, []);

  const earnedCount = useMemo(
    () => overview?.achievements.filter((item) => item.earned).length ?? 0,
    [overview]
  );

  async function saveGoal() {
    setWorking(true);
    setError(null);

    try {
      const result = await updateMealLoggingGoal(target);
      setOverview(result);
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not update the goal."
      );
    } finally {
      setWorking(false);
    }
  }

  async function toggleGoal() {
    if (!overview?.mealLoggingGoal) return;

    setWorking(true);
    setError(null);

    try {
      const result =
        overview.mealLoggingGoal.status === "PAUSED"
          ? await resumeMealLoggingGoal()
          : await pauseMealLoggingGoal();
      setOverview(result);
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not update the goal."
      );
    } finally {
      setWorking(false);
    }
  }

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Progress"
        title="Build enough consistency to learn from."
        description="Progress here measures whether Aarogya has useful repeated data. It does not score the quality of your diet, body weight or discipline."
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

      {loading || !overview ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">A</div>
          <p>Checking your consistency…</p>
        </div>
      ) : (
        <>
          <section className="progressHero">
            <div>
              <span className="cardEyebrow">Current rhythm</span>
              <h2>
                {overview.streak.currentRunDays > 0
                  ? `${overview.streak.currentRunDays}-day run`
                  : "No active run"}
              </h2>
              <p>{overview.streak.message}</p>
            </div>

            <dl>
              <div>
                <dt>Longest run</dt>
                <dd>{overview.streak.longestRunDays} days</dd>
              </div>
              <div>
                <dt>Total logging days</dt>
                <dd>{overview.streak.totalLoggingDays}</dd>
              </div>
              <div>
                <dt>Today</dt>
                <dd>{overview.streak.todayLogged ? "Recorded" : "Still open"}</dd>
              </div>
              <div>
                <dt>Milestones</dt>
                <dd>{earnedCount}/{overview.achievements.length}</dd>
              </div>
            </dl>
          </section>

          <div className="progressWorkspace">
            <section className="goalPanel">
              <div className="goalPanel__heading">
                <div>
                  <span className="cardEyebrow">Weekly goal</span>
                  <h2>Keep a useful meal record</h2>
                </div>
                <StatusChip
                  tone={
                    overview.mealLoggingGoal?.status === "PAUSED"
                      ? "warm"
                      : "positive"
                  }
                >
                  {overview.mealLoggingGoal?.status === "PAUSED"
                    ? "Paused"
                    : overview.mealLoggingGoal
                      ? "Active"
                      : "Optional"}
                </StatusChip>
              </div>

              {overview.mealLoggingGoal ? (
                <>
                  <p className="goalPanel__copy">
                    {overview.mealLoggingGoal.description}
                  </p>

                  <div className="goalProgressBlock">
                    <div className="goalProgressBlock__labels">
                      <strong>
                        {overview.mealLoggingGoal.currentValue} of{" "}
                        {overview.mealLoggingGoal.targetValue} chosen days
                      </strong>
                      <span>
                        {formatWeek(
                          overview.mealLoggingGoal.periodStart,
                          overview.mealLoggingGoal.periodEnd
                        )}
                      </span>
                    </div>
                    <ProgressBar
                      value={overview.mealLoggingGoal.progressPercent}
                      label={overview.mealLoggingGoal.progressLabel}
                    />
                    <p>{overview.mealLoggingGoal.progressLabel}</p>
                  </div>

                  <div className="goalControls">
                    <label>
                      <span>Days per week</span>
                      <select
                        value={target}
                        disabled={working}
                        onChange={(event) =>
                          setTarget(Number(event.target.value))
                        }
                      >
                        {[2, 3, 4, 5, 6, 7].map((value) => (
                          <option key={value} value={value}>
                            {value} days
                          </option>
                        ))}
                      </select>
                    </label>

                    <button
                      className="button button--primary"
                      type="button"
                      disabled={
                        working
                        || target === overview.mealLoggingGoal.targetValue
                      }
                      onClick={() => void saveGoal()}
                    >
                      Update target
                    </button>

                    <button
                      className="button button--ghost"
                      type="button"
                      disabled={working}
                      onClick={() => void toggleGoal()}
                    >
                      {overview.mealLoggingGoal.status === "PAUSED"
                        ? "Resume goal"
                        : "Pause goal"}
                    </button>
                  </div>
                </>
              ) : (
                <div className="goalStarter">
                  <p>
                    Choose how many days this week you would like to record at
                    least one meal. The target measures consistency only.
                  </p>

                  <div className="goalControls">
                    <label>
                      <span>Days per week</span>
                      <select
                        value={target}
                        disabled={working}
                        onChange={(event) =>
                          setTarget(Number(event.target.value))
                        }
                      >
                        {[2, 3, 4, 5, 6, 7].map((value) => (
                          <option key={value} value={value}>
                            {value} days
                          </option>
                        ))}
                      </select>
                    </label>

                    <button
                      className="button button--primary"
                      type="button"
                      disabled={working}
                      onClick={() => void saveGoal()}
                    >
                      Set weekly goal
                    </button>
                  </div>
                </div>
              )}
            </section>

            <aside className="progressPrincipleCard">
              <span className="cardEyebrow">No perfect streaks</span>
              <h2>A missed day does not erase progress.</h2>
              <p>{overview.philosophy}</p>
              <div className="progressPrincipleCard__note">
                Your current run stays intact through today when yesterday was
                logged, so opening Aarogya in the morning is never treated as a
                failure.
              </div>
            </aside>
          </div>

          <section className="achievementSection">
            <div className="achievementSection__heading">
              <div>
                <span className="cardEyebrow">Milestones</span>
                <h2>Small markers of useful data</h2>
              </div>
              <span>{earnedCount} earned</span>
            </div>

            <div className="achievementGrid">
              {overview.achievements.map((achievement) => (
                <article
                  className={
                    achievement.earned
                      ? "achievementCard is-earned"
                      : "achievementCard"
                  }
                  key={achievement.code}
                >
                  <div className="achievementCard__mark" aria-hidden="true">
                    {achievement.earned ? "✓" : "·"}
                  </div>
                  <span className="cardEyebrow">
                    {achievement.earned ? "Earned" : "Not yet"}
                  </span>
                  <h3>{achievement.title}</h3>
                  <p>{achievement.description}</p>
                  <small>
                    {achievement.earned && achievement.earnedAt
                      ? `Earned ${formatDate(achievement.earnedAt)}`
                      : milestoneLabel(
                          achievement.criteriaCode,
                          achievement.thresholdValue
                        )}
                  </small>
                </article>
              ))}
            </div>
          </section>

          <section className="progressBoundary">
            <span className="cardEyebrow">Phase boundary</span>
            <h2>Phase 12 measures consistency. Phase 13 will analyze trends.</h2>
            <p>
              This screen deliberately avoids calorie targets, weight-loss
              rewards and nutrient perfection scores. Longitudinal nutrition
              charts stay in a separate workspace so consistency and analytics
              do not compete for attention.
            </p>
            <Link
              className="button button--secondary progressAnalyticsLink"
              href="/analytics"
            >
              Open longitudinal analytics
            </Link>
          </section>
        </>
      )}
    </main>
  );
}

function milestoneLabel(criteriaCode: string, threshold: number) {
  return criteriaCode === "LONGEST_LOGGING_RUN"
    ? `${threshold}-day logging run`
    : `${threshold} total logging days`;
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric"
  }).format(new Date(value));
}

function formatWeek(start: string, end: string) {
  const formatter = new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short"
  });
  return `${formatter.format(new Date(start))} – ${formatter.format(
    new Date(end)
  )}`;
}

function todayKey() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

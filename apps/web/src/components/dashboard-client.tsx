"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { useAuth } from "@/components/auth-gate";
import { PageHeader } from "@/components/page-header";
import { StatusChip, Surface } from "@/components/ui";
import { getMealDay, type DailyMealLog } from "@/lib/meals";
import { getProfile, type Profile } from "@/lib/profile";
import {
  announceNudgeChange,
  evaluateNudges,
  getNudges,
  type Nudge
} from "@/lib/nudges";

export function DashboardClient() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [mealDay, setMealDay] = useState<DailyMealLog | null>(null);
  const [nudges, setNudges] = useState<Nudge[] | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    Promise.all([
      getProfile(),
      getMealDay(todayKey()),
      evaluateNudges(todayKey()).then(async () => {
        const next = await getNudges(false);
        announceNudgeChange();
        return next;
      })
    ])
      .then(([profileResult, mealResult, nudgeResult]) => {
        setProfile(profileResult);
        setMealDay(mealResult);
        setNudges(nudgeResult);
      })
      .catch(() => setFailed(true));
  }, []);

  const totals = useMemo(() => {
    const values = new Map((mealDay?.totals ?? []).map((item) => [item.code, item.amount]));
    return {
      energy: values.get("ENERGY_KCAL") ?? 0,
      protein: values.get("PROTEIN_G") ?? 0,
      fibre: values.get("FIBRE_G") ?? 0
    };
  }, [mealDay]);

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Today"
        title={`Good morning, ${firstName(user.displayName)}.`}
        description="Today combines your real meal log with a restrained attention layer. Detailed reasoning stays one click away."
      />

      {failed ? (
        <div className="formNotice formNotice--error" role="alert">
          We could not load today&apos;s workspace right now.
        </div>
      ) : null}

      {!profile || !mealDay || nudges === null ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">A</div>
          <p>Loading today&apos;s workspace…</p>
        </div>
      ) : !profile.onboardingComplete ? (
        <section className="setupHero">
          <div>
            <StatusChip tone="warm">Profile setup incomplete</StatusChip>
            <span className="dashboardHero__label">Your next useful action</span>
            <h2>Give Aarogya just enough context to personalize responsibly.</h2>
            <p>
              Complete the short profile flow for dietary pattern, activity, goals and optional safety context.
            </p>
          </div>
          <Link className="button button--primary" href="/onboarding">
            Set up my profile
          </Link>
        </section>
      ) : (
        <>
          <section className="setupHero setupHero--complete">
            <div>
              <StatusChip tone={mealDay.entryCount > 0 ? "positive" : "warm"}>
                {mealDay.entryCount > 0 ? "Meals logged today" : "No meals logged yet"}
              </StatusChip>
              <span className="dashboardHero__label">Today&apos;s nutrition record</span>
              <h2>
                {mealDay.entryCount > 0
                  ? "Your daily totals now come from foods you actually logged."
                  : "Start with one meal. Aarogya will build the day from there."}
              </h2>
              <p>
                {mealDay.entryCount > 0
                  ? `${mealDay.entryCount} ${mealDay.entryCount === 1 ? "food entry" : "food entries"} recorded. Historical nutrient values are frozen at log time.`
                  : "Use the source-aware food catalog to add breakfast, lunch, dinner or snacks."}
              </p>
            </div>
            <Link className="button button--primary" href="/meals">
              {mealDay.entryCount > 0 ? "Review meals" : "Log a meal"}
            </Link>
          </section>

          <div className="dashboardGrid dashboardGrid--phase4">
            <Surface className="dashboardCard">
              <span className="cardEyebrow">Today&apos;s totals</span>
              <h2 className="profileSectionTitle">
                {mealDay.entryCount > 0 ? "Recorded nutrition" : "Waiting for your first meal"}
              </h2>
              <dl className="profileDetails profileDetails--compact">
                <Detail label="Energy" value={`${formatNumber(totals.energy)} kcal`} />
                <Detail label="Protein" value={`${formatNumber(totals.protein)} g`} />
                <Detail label="Fibre" value={`${formatNumber(totals.fibre)} g`} />
                <Detail label="Entries" value={String(mealDay.entryCount)} />
              </dl>
            </Surface>

            <Surface className="dashboardCard">
              <span className="cardEyebrow">Your context</span>
              <h2 className="profileSectionTitle">Profile foundation</h2>
              <dl className="profileDetails profileDetails--compact">
                <Detail label="Diet" value={pretty(profile.dietaryPattern)} />
                <Detail label="Activity" value={pretty(profile.activityLevel)} />
                <Detail label="Goals" value={String(profile.goals.length)} />
                <Detail label="Region" value={profile.stateOrRegion ?? "Not provided"} />
              </dl>
            </Surface>

            <Surface className="dashboardCard">
              <span className="cardEyebrow">Needs your attention</span>
              <h2 className="profileSectionTitle">
                {nudges.find((item) => item.status === "ACTIVE")?.title
                  ?? "Nothing is being pushed right now"}
              </h2>
              <p className="dashboardTruthCopy">
                {nudges.find((item) => item.status === "ACTIVE")?.message
                  ?? "Aarogya has no active nudge. Guidance remains available when you want to inspect it."}
              </p>
              <div className="dashboardGuidanceFooter">
                <StatusChip
                  tone={
                    nudges.some(
                      (item) =>
                        item.status === "ACTIVE"
                        && item.severity === "ATTENTION"
                    )
                      ? "attention"
                      : nudges.some((item) => item.status === "ACTIVE")
                        ? "warm"
                        : "positive"
                  }
                >
                  {nudges.filter((item) => item.status === "ACTIVE").length > 0
                    ? `${nudges.filter((item) => item.status === "ACTIVE").length} active`
                    : "clear"}
                </StatusChip>
                <Link className="quietLink" href="/alerts">
                  Review alerts
                </Link>
              </div>
            </Surface>
          </div>
        </>
      )}
    </main>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}

function firstName(displayName: string) {
  return displayName.trim().split(/\s+/)[0] || "there";
}

function pretty(value: string | null) {
  if (!value) return "Not provided";
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
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

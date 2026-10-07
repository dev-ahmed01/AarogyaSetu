"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { useAuth } from "@/components/auth-gate";
import { PageHeader } from "@/components/page-header";
import { StatusChip, Surface } from "@/components/ui";
import { getMealDay, type DailyMealLog } from "@/lib/meals";
import { getProfile, type Profile } from "@/lib/profile";
import {
  getTodayRecommendations,
  type RecommendationAssessment
} from "@/lib/recommendations";

export function DashboardClient() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [mealDay, setMealDay] = useState<DailyMealLog | null>(null);
  const [guidance, setGuidance] = useState<RecommendationAssessment | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    Promise.all([
      getProfile(),
      getMealDay(todayKey()),
      getTodayRecommendations(todayKey())
    ])
      .then(([profileResult, mealResult, guidanceResult]) => {
        setProfile(profileResult);
        setMealDay(mealResult);
        setGuidance(guidanceResult);
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
        description="Today now reflects real profile and meal-log data. Personalized recommendations stay separate until their evidence rules are built."
      />

      {failed ? (
        <div className="formNotice formNotice--error" role="alert">
          We could not load today&apos;s workspace right now.
        </div>
      ) : null}

      {!profile || !mealDay || !guidance ? (
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
              <span className="cardEyebrow">Guidance</span>
              <h2 className="profileSectionTitle">
                {guidance.recommendations[0]?.title ?? "No recommendation forced"}
              </h2>
              <p className="dashboardTruthCopy">
                {guidance.recommendations[0]?.observation
                  ?? guidance.notices[guidance.notices.length - 1]
                  ?? "Aarogya has no explainable guidance to surface yet."}
              </p>
              <div className="dashboardGuidanceFooter">
                <StatusChip
                  tone={guidance.status.includes("SAFETY") ? "attention" : "positive"}
                >
                  {guidance.status.replaceAll("_", " ").toLowerCase()}
                </StatusChip>
                <Link className="quietLink" href="/guidance">
                  Why this?
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

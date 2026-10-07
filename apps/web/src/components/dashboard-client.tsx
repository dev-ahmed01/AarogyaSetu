"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { useAuth } from "@/components/auth-gate";
import { PageHeader } from "@/components/page-header";
import { StatusChip, Surface } from "@/components/ui";
import { getProfile, type Profile } from "@/lib/profile";

export function DashboardClient() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    getProfile()
      .then(setProfile)
      .catch(() => setFailed(true));
  }, []);

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Today"
        title={`Good morning, ${firstName(user.displayName)}.`}
        description="Only information backed by your account and profile appears here now. Nutrition metrics arrive after meal tracking is built."
      />

      {failed ? (
        <div className="formNotice formNotice--error" role="alert">
          We could not load your profile right now.
        </div>
      ) : null}

      {!profile ? (
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
              Complete five short steps for dietary pattern, activity, goals and optional safety context.
              Nothing is stored until you explicitly agree to personalization.
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
              <StatusChip tone={profile.personalizationConsentGranted ? "positive" : "attention"}>
                {profile.personalizationConsentGranted ? "Profile ready" : "Personalization paused"}
              </StatusChip>
              <span className="dashboardHero__label">Profile foundation</span>
              <h2>
                {profile.personalizationConsentGranted
                  ? "Your context is ready for the nutrition layers that come next."
                  : "Your stored context is visible, but personalization is currently paused."}
              </h2>
              <p>
                {profile.personalizationConsentGranted
                  ? "Meal logging and the nutrition engine will build on this profile without inventing data you have not entered."
                  : "Re-enable consent from your profile before Aarogya uses stored context for personalized guidance."}
              </p>
            </div>
            <Link className="button button--secondary" href="/profile">
              Review profile
            </Link>
          </section>

          <div className="dashboardGrid dashboardGrid--phase4">
            <Surface className="dashboardCard">
              <span className="cardEyebrow">Your context</span>
              <h2 className="profileSectionTitle">Current foundation</h2>
              <dl className="profileDetails profileDetails--compact">
                <Detail label="Diet" value={pretty(profile.dietaryPattern)} />
                <Detail label="Activity" value={pretty(profile.activityLevel)} />
                <Detail label="Goals" value={String(profile.goals.length)} />
                <Detail label="Region" value={profile.stateOrRegion ?? "Not provided"} />
              </dl>
            </Surface>

            <Surface className="dashboardCard">
              <span className="cardEyebrow">Meals</span>
              <h2 className="profileSectionTitle">No fabricated meal data</h2>
              <p className="dashboardTruthCopy">
                Meal tracking is not implemented yet, so this dashboard deliberately shows no calories,
                fibre totals or nutrition score.
              </p>
              <StatusChip>Meal logging comes next</StatusChip>
            </Surface>

            <Surface className="dashboardCard">
              <span className="cardEyebrow">Recommendations</span>
              <h2 className="profileSectionTitle">Waiting for real inputs</h2>
              <p className="dashboardTruthCopy">
                Personalized recommendations remain off until food data and the rule/evidence layer are available.
              </p>
              <StatusChip>Explainability preserved</StatusChip>
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

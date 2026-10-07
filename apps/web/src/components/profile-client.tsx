"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { AccountDataControls } from "@/components/account-data-controls";
import { PageHeader } from "@/components/page-header";
import { StatusChip, Surface } from "@/components/ui";
import {
  getProfile,
  updatePersonalizationConsent,
  type Profile
} from "@/lib/profile";
import {
  getResearchConsent,
  updateResearchConsent,
  type ResearchConsent
} from "@/lib/research";

export function ProfileClient() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [updatingConsent, setUpdatingConsent] = useState(false);
  const [researchConsent, setResearchConsent] =
    useState<ResearchConsent | null>(null);
  const [updatingResearch, setUpdatingResearch] = useState(false);

  useEffect(() => {
    Promise.all([
      getProfile(),
      getResearchConsent()
    ])
      .then(([nextProfile, nextResearchConsent]) => {
        setProfile(nextProfile);
        setResearchConsent(nextResearchConsent);
      })
      .catch((cause) =>
        setError(cause instanceof Error ? cause.message : "Could not load your profile.")
      );
  }, []);

  async function revokeConsent() {
    setUpdatingConsent(true);
    setError(null);

    try {
      const updated = await updatePersonalizationConsent(false);
      setProfile(updated);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not update consent.");
    } finally {
      setUpdatingConsent(false);
    }
  }

  async function toggleResearchConsent() {
    if (!researchConsent) return;

    setUpdatingResearch(true);
    setError(null);

    try {
      setResearchConsent(
        await updateResearchConsent(!researchConsent.granted)
      );
    } catch (cause) {
      setError(
        cause instanceof Error
          ? cause.message
          : "Could not update research participation."
      );
    } finally {
      setUpdatingResearch(false);
    }
  }

  if (!profile) {
    return (
      <div className="profileLoading">
        <div className="sessionLoading__mark">A</div>
        <p>{error ?? "Loading your profile…"}</p>
      </div>
    );
  }

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Your profile"
        title="Personal context, kept understandable."
        description="Review what Aarogya currently knows and whether it is allowed to use that context for personalization."
        action={
          <Link className="button button--secondary" href="/onboarding">
            Edit profile
          </Link>
        }
      />

      {error ? <div className="formNotice formNotice--error" role="alert">{error}</div> : null}

      <div className="profileGrid">
        <Surface className="profileCard profileCard--wide">
          <div className="cardHeader">
            <div>
              <span className="cardEyebrow">Personalization</span>
              <h2>Current context</h2>
            </div>
            <StatusChip tone={profile.onboardingComplete ? "positive" : "warm"}>
              {profile.onboardingComplete ? "Setup complete" : "Setup incomplete"}
            </StatusChip>
          </div>

          <dl className="profileDetails">
            <Detail label="Age" value={profile.ageYears ? String(profile.ageYears) : "Not provided"} />
            <Detail label="Dietary pattern" value={pretty(profile.dietaryPattern)} />
            <Detail label="Activity" value={pretty(profile.activityLevel)} />
            <Detail label="State / region" value={profile.stateOrRegion ?? "Not provided"} />
            <Detail label="Height" value={profile.heightCm ? `${profile.heightCm} cm` : "Not provided"} />
            <Detail label="Weight" value={profile.weightKg ? `${profile.weightKg} kg` : "Not provided"} />
          </dl>
          {profile.stateOrRegion ? (
            <Link className="quietLink profileRegionalLink" href="/regional">
              Explore regional food context
            </Link>
          ) : null}
        </Surface>

        <Surface className="profileCard">
          <div className="cardHeader">
            <div>
              <span className="cardEyebrow">Consent</span>
              <h2>Personalization permission</h2>
            </div>
          </div>

          <div className="consentStatus">
            <StatusChip tone={profile.personalizationConsentGranted ? "positive" : "attention"}>
              {profile.personalizationConsentGranted ? "Granted" : "Paused"}
            </StatusChip>
            <p>
              {profile.personalizationConsentGranted
                ? "Aarogya may use this profile for personalized wellness and nutrition guidance."
                : "Stored profile context must not be used for personalization while consent is paused."}
            </p>
          </div>

          {profile.personalizationConsentGranted ? (
            <button
              className="button button--ghost profileConsentAction"
              type="button"
              disabled={updatingConsent}
              onClick={() => void revokeConsent()}
            >
              {updatingConsent ? "Updating…" : "Pause personalization"}
            </button>
          ) : (
            <Link className="button button--secondary profileConsentAction" href="/onboarding">
              Review & re-enable
            </Link>
          )}

          <p className="cardFootnote">
            Policy version {profile.consentPolicyVersion ?? "—"}.
            Revoking consent does not delete stored data in this prototype; data-deletion controls are part of production hardening.
          </p>
        </Surface>

        <Surface className="profileCard">
          <div className="cardHeader">
            <div>
              <span className="cardEyebrow">Research participation</span>
              <h2>Optional evaluation consent</h2>
            </div>
            <StatusChip
              tone={researchConsent?.granted ? "positive" : "neutral"}
            >
              {researchConsent?.granted ? "Opted in" : "Not participating"}
            </StatusChip>
          </div>

          <div className="consentStatus">
            <p>
              {researchConsent?.granted
                ? "Aarogya may include de-identified product-use and meal-logging patterns in aggregate research evaluation. Small cohorts are suppressed."
                : "Research evaluation is separate from personalization. Nothing is included unless you opt in."}
            </p>
          </div>

          <button
            className={
              researchConsent?.granted
                ? "button button--ghost profileConsentAction"
                : "button button--secondary profileConsentAction"
            }
            type="button"
            disabled={!researchConsent || updatingResearch}
            onClick={() => void toggleResearchConsent()}
          >
            {updatingResearch
              ? "Updating…"
              : researchConsent?.granted
                ? "Leave research evaluation"
                : "Opt in to research evaluation"}
          </button>

          <p className="cardFootnote">
            Feature exposure is recorded at most once per feature per day.
            Leaving research evaluation deletes Aarogya's research-event
            instrumentation for your account and excludes you from future
            aggregate calculations. Consent policy{" "}
            {researchConsent?.policyVersion ?? "—"}.
          </p>
        </Surface>

        <AccountDataControls />

        <Surface className="profileCard">
          <span className="cardEyebrow">Goals</span>
          <h2 className="profileSectionTitle">What you want help with</h2>
          <div className="profileTags">
            {profile.goals.length > 0
              ? profile.goals.map((goal) => <span key={goal}>{pretty(goal)}</span>)
              : <p className="profileEmptyCopy">No goals selected.</p>}
          </div>
        </Surface>

        <Surface className="profileCard">
          <span className="cardEyebrow">Safety context</span>
          <h2 className="profileSectionTitle">Self-reported details</h2>
          <div className="profileSubsection">
            <strong>Allergies</strong>
            <div className="profileTags">
              {profile.allergies.length > 0
                ? profile.allergies.map((item) => <span key={item}>{pretty(item)}</span>)
                : <p className="profileEmptyCopy">None provided.</p>}
            </div>
          </div>
          <div className="profileSubsection">
            <strong>Health context</strong>
            <div className="profileTags">
              {profile.healthContexts.length > 0
                ? profile.healthContexts.map((item) => <span key={item}>{pretty(item)}</span>)
                : <p className="profileEmptyCopy">None provided.</p>}
            </div>
          </div>
          <p className="cardFootnote">
            These are user-provided context, not diagnoses made by Aarogya.
          </p>
        </Surface>
      </div>
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

function pretty(value: string | null) {
  if (!value) return "Not provided";
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";

import {
  completeOnboarding,
  getProfile,
  updatePersonalizationConsent,
  updateProfile,
  type ProfileInput
} from "@/lib/profile";

const steps = [
  "About you",
  "Daily rhythm",
  "Goals",
  "Safety context",
  "Consent"
] as const;

const activityOptions = [
  ["SEDENTARY", "Mostly seated"],
  ["LIGHT", "Lightly active"],
  ["MODERATE", "Moderately active"],
  ["HIGH", "Highly active"],
  ["VERY_HIGH", "Very highly active"]
] as const;

const dietOptions = [
  ["VEGETARIAN", "Vegetarian"],
  ["VEGAN", "Vegan"],
  ["EGGETARIAN", "Eggetarian"],
  ["NON_VEGETARIAN", "Non-vegetarian"],
  ["PESCATARIAN", "Pescatarian"],
  ["JAIN", "Jain"],
  ["OTHER", "Other"]
] as const;

const goalOptions = [
  ["BALANCED_NUTRITION", "Eat more balanced meals"],
  ["WEIGHT_MANAGEMENT", "Support weight management"],
  ["MUSCLE_GAIN", "Support muscle gain"],
  ["ENERGY", "Support everyday energy"],
  ["HEART_HEALTH", "Choose more heart-conscious foods"]
] as const;

const allergyOptions = [
  ["PEANUT", "Peanut"],
  ["TREE_NUT", "Tree nuts"],
  ["MILK", "Milk"],
  ["EGG", "Egg"],
  ["WHEAT", "Wheat"],
  ["SOY", "Soy"],
  ["SESAME", "Sesame"],
  ["FISH", "Fish"],
  ["SHELLFISH", "Shellfish"]
] as const;

const contextOptions = [
  ["DIABETES", "Diabetes"],
  ["HYPERTENSION", "High blood pressure"],
  ["ANEMIA", "Anaemia"],
  ["HIGH_CHOLESTEROL", "High cholesterol"],
  ["PCOS", "PCOS"],
  ["THYROID_CONDITION", "Thyroid condition"],
  ["KIDNEY_CONDITION", "Kidney condition"],
  ["PREGNANCY_OR_BREASTFEEDING", "Pregnancy or breastfeeding"],
  ["OTHER", "Other"]
] as const;

const stateOptions = [
  "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
  "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
  "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
  "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
  "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
  "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu",
  "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
];

const emptyProfile: ProfileInput = {
  ageYears: null,
  sexForNutrition: null,
  heightCm: null,
  weightKg: null,
  activityLevel: null,
  dietaryPattern: null,
  stateOrRegion: null,
  goals: [],
  allergies: [],
  healthContexts: []
};

export function OnboardingWizard() {
  const router = useRouter();
  const [step, setStep] = useState(0);
  const [form, setForm] = useState<ProfileInput>(emptyProfile);
  const [consent, setConsent] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    getProfile()
      .then((profile) => {
        if (cancelled) return;
        setForm({
          ageYears: profile.ageYears,
          sexForNutrition: profile.sexForNutrition,
          heightCm: profile.heightCm,
          weightKg: profile.weightKg,
          activityLevel: profile.activityLevel,
          dietaryPattern: profile.dietaryPattern,
          stateOrRegion: profile.stateOrRegion,
          goals: profile.goals,
          allergies: profile.allergies,
          healthContexts: profile.healthContexts
        });
        setConsent(profile.personalizationConsentGranted);
      })
      .catch((cause) => {
        if (!cancelled) {
          setError(cause instanceof Error ? cause.message : "Could not load your profile.");
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const progress = useMemo(
    () => Math.round(((step + 1) / steps.length) * 100),
    [step]
  );

  function toggleSet(field: "goals" | "allergies" | "healthContexts", value: string) {
    setForm((current) => {
      const values = current[field];
      return {
        ...current,
        [field]: values.includes(value)
          ? values.filter((item) => item !== value)
          : [...values, value]
      };
    });
  }

  function next() {
    setError(null);

    if (step === 0 && (form.ageYears === null || form.ageYears < 13 || form.ageYears > 120)) {
      setError("Enter an age between 13 and 120.");
      return;
    }

    if (step === 1 && (!form.activityLevel || !form.dietaryPattern)) {
      setError("Choose your activity level and dietary pattern.");
      return;
    }

    if (step === 2 && form.goals.length === 0) {
      setError("Choose at least one goal.");
      return;
    }

    setStep((current) => Math.min(current + 1, steps.length - 1));
  }

  async function finish() {
    if (!consent) {
      setError("Consent is required to store this profile for personalized guidance.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      await updatePersonalizationConsent(true);
      await updateProfile(form);
      await completeOnboarding();
      router.replace("/dashboard");
      router.refresh();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not finish setup.");
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div className="onboardingLoading">
        <div className="sessionLoading__mark">A</div>
        <p>Preparing your profile…</p>
      </div>
    );
  }

  return (
    <div className="onboardingLayout">
      <aside className="onboardingRail">
        <span className="sectionLabel">Profile setup</span>
        <h1>Build only the context Aarogya actually needs.</h1>
        <p>
          Short steps, no exact location, no full date of birth, and optional
          health context that is always labelled as self-reported.
        </p>

        <div className="onboardingProgress" aria-label={`Setup progress ${progress}%`}>
          <span style={{ width: `${progress}%` }} />
        </div>

        <ol className="onboardingSteps">
          {steps.map((label, index) => (
            <li
              className={index === step ? "is-active" : index < step ? "is-done" : ""}
              key={label}
            >
              <span>{String(index + 1).padStart(2, "0")}</span>
              {label}
            </li>
          ))}
        </ol>

        <button className="quietLink onboardingSkip" type="button" onClick={() => router.push("/dashboard")}>
          I&apos;ll do this later
        </button>
      </aside>

      <section className="onboardingPanel">
        <div className="onboardingCard">
          <span className="pageHeader__eyebrow">Step {step + 1} of {steps.length}</span>

          {step === 0 ? (
            <>
              <h2>Start with the basics.</h2>
              <p className="onboardingIntro">
                These details help later nutrition calculations. Only age is required here.
              </p>

              <div className="onboardingFormGrid">
                <label className="formField">
                  <span className="formField__label">Age</span>
                  <input
                    className="formField__control"
                    type="number"
                    min={13}
                    max={120}
                    value={form.ageYears ?? ""}
                    onChange={(event) =>
                      setForm({ ...form, ageYears: event.target.value ? Number(event.target.value) : null })
                    }
                    placeholder="e.g. 24"
                  />
                </label>

                <label className="formField">
                  <span className="formField__label">Sex used for nutrition calculations</span>
                  <select
                    className="formField__control"
                    value={form.sexForNutrition ?? ""}
                    onChange={(event) =>
                      setForm({ ...form, sexForNutrition: event.target.value || null })
                    }
                  >
                    <option value="">Prefer not to say / skip</option>
                    <option value="FEMALE">Female</option>
                    <option value="MALE">Male</option>
                    <option value="INTERSEX_OR_OTHER">Intersex / another option</option>
                    <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
                  </select>
                  <span className="formField__hint">Optional. Used only where a calculation genuinely needs it.</span>
                </label>

                <label className="formField">
                  <span className="formField__label">Height (cm)</span>
                  <input
                    className="formField__control"
                    type="number"
                    min={80}
                    max={250}
                    step="0.1"
                    value={form.heightCm ?? ""}
                    onChange={(event) =>
                      setForm({ ...form, heightCm: event.target.value ? Number(event.target.value) : null })
                    }
                    placeholder="Optional"
                  />
                </label>

                <label className="formField">
                  <span className="formField__label">Weight (kg)</span>
                  <input
                    className="formField__control"
                    type="number"
                    min={20}
                    max={350}
                    step="0.1"
                    value={form.weightKg ?? ""}
                    onChange={(event) =>
                      setForm({ ...form, weightKg: event.target.value ? Number(event.target.value) : null })
                    }
                    placeholder="Optional"
                  />
                </label>
              </div>
            </>
          ) : null}

          {step === 1 ? (
            <>
              <h2>What does everyday life look like?</h2>
              <p className="onboardingIntro">
                Broad context is enough. Aarogya does not need your precise location.
              </p>

              <div className="onboardingFormGrid">
                <label className="formField">
                  <span className="formField__label">Activity level</span>
                  <select
                    className="formField__control"
                    value={form.activityLevel ?? ""}
                    onChange={(event) =>
                      setForm({ ...form, activityLevel: event.target.value || null })
                    }
                  >
                    <option value="">Choose one</option>
                    {activityOptions.map(([value, label]) => (
                      <option value={value} key={value}>{label}</option>
                    ))}
                  </select>
                </label>

                <label className="formField">
                  <span className="formField__label">Dietary pattern</span>
                  <select
                    className="formField__control"
                    value={form.dietaryPattern ?? ""}
                    onChange={(event) =>
                      setForm({ ...form, dietaryPattern: event.target.value || null })
                    }
                  >
                    <option value="">Choose one</option>
                    {dietOptions.map(([value, label]) => (
                      <option value={value} key={value}>{label}</option>
                    ))}
                  </select>
                </label>

                <label className="formField onboardingFull">
                  <span className="formField__label">State or region</span>
                  <select
                    className="formField__control"
                    value={form.stateOrRegion ?? ""}
                    onChange={(event) =>
                      setForm({ ...form, stateOrRegion: event.target.value || null })
                    }
                  >
                    <option value="">Optional</option>
                    {stateOptions.map((state) => (
                      <option key={state} value={state}>{state}</option>
                    ))}
                  </select>
                  <span className="formField__hint">
                    Used later for regional food relevance, not for tracking your location.
                  </span>
                </label>
              </div>
            </>
          ) : null}

          {step === 2 ? (
            <>
              <h2>What would you like help with?</h2>
              <p className="onboardingIntro">
                Choose what matters now. You can change these later.
              </p>
              <div className="choiceGrid">
                {goalOptions.map(([value, label]) => (
                  <button
                    className={form.goals.includes(value) ? "choiceCard is-selected" : "choiceCard"}
                    type="button"
                    key={value}
                    onClick={() => toggleSet("goals", value)}
                  >
                    <span>{label}</span>
                    <small>{form.goals.includes(value) ? "Selected" : "Choose"}</small>
                  </button>
                ))}
              </div>
            </>
          ) : null}

          {step === 3 ? (
            <>
              <h2>Add safety context only if you want to.</h2>
              <p className="onboardingIntro">
                These are self-reported details, not diagnoses made by Aarogya.
                They will be used later to avoid unsuitable suggestions and surface caution.
              </p>

              <div className="choiceSection">
                <h3>Food allergies</h3>
                <div className="choicePills">
                  {allergyOptions.map(([value, label]) => (
                    <button
                      className={form.allergies.includes(value) ? "choicePill is-selected" : "choicePill"}
                      type="button"
                      key={value}
                      onClick={() => toggleSet("allergies", value)}
                    >
                      {label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="choiceSection">
                <h3>Self-reported health context</h3>
                <div className="choicePills">
                  {contextOptions.map(([value, label]) => (
                    <button
                      className={form.healthContexts.includes(value) ? "choicePill is-selected" : "choicePill"}
                      type="button"
                      key={value}
                      onClick={() => toggleSet("healthContexts", value)}
                    >
                      {label}
                    </button>
                  ))}
                </div>
              </div>
            </>
          ) : null}

          {step === 4 ? (
            <>
              <h2>You stay in control.</h2>
              <p className="onboardingIntro">
                Aarogya can store this profile and use it to personalize wellness and nutrition guidance.
                It does not authorize diagnosis or treatment decisions.
              </p>

              <div className="consentBox">
                <label>
                  <input
                    type="checkbox"
                    checked={consent}
                    onChange={(event) => setConsent(event.target.checked)}
                  />
                  <span>
                    <strong>I agree to personalized profile processing.</strong>
                    <small>
                      I understand the profile may include optional self-reported health context and
                      I can later revoke this consent from my profile.
                    </small>
                  </span>
                </label>
                <p>Policy version 2026-10 · consent is stored as a separate audit record.</p>
              </div>

              <div className="reviewGrid">
                <div><span>Age</span><strong>{form.ageYears ?? "—"}</strong></div>
                <div><span>Diet</span><strong>{pretty(form.dietaryPattern)}</strong></div>
                <div><span>Activity</span><strong>{pretty(form.activityLevel)}</strong></div>
                <div><span>Goals</span><strong>{form.goals.length}</strong></div>
              </div>
            </>
          ) : null}

          {error ? <div className="formNotice formNotice--error" role="alert">{error}</div> : null}

          <div className="onboardingActions">
            <button
              className="button button--secondary"
              type="button"
              disabled={step === 0 || submitting}
              onClick={() => setStep((current) => Math.max(current - 1, 0))}
            >
              Back
            </button>

            {step < steps.length - 1 ? (
              <button className="button button--primary" type="button" onClick={next}>
                Continue
              </button>
            ) : (
              <button
                className="button button--primary"
                type="button"
                disabled={submitting}
                onClick={() => void finish()}
              >
                {submitting ? "Saving profile…" : "Agree & finish setup"}
              </button>
            )}
          </div>
        </div>
      </section>
    </div>
  );
}

function pretty(value: string | null) {
  if (!value) return "—";
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

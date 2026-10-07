"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import {
  archivePlan,
  generatePlan,
  getPlanDay,
  getSmartSuggestions,
  type DietPlan,
  type DietPlanItem,
  type SmartFoodSuggestion
} from "@/lib/plans";

const mealLabels: Record<string, string> = {
  BREAKFAST: "Breakfast",
  LUNCH: "Lunch",
  DINNER: "Dinner",
  SNACK: "Snack"
};

export function PlansClient() {
  const [date, setDate] = useState(todayKey());
  const [plan, setPlan] = useState<DietPlan | null>(null);
  const [suggestions, setSuggestions] = useState<SmartFoodSuggestion[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [suggestionError, setSuggestionError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function load(target = date) {
    setLoading(true);
    setError(null);
    setSuggestionError(null);

    const [dayResult, suggestionResult] = await Promise.allSettled([
      getPlanDay(target),
      getSmartSuggestions(target)
    ]);

    if (dayResult.status === "fulfilled") {
      setPlan(dayResult.value.plan);
    } else {
      setError(
        dayResult.reason instanceof Error
          ? dayResult.reason.message
          : "Could not load the draft plan."
      );
    }

    if (suggestionResult.status === "fulfilled") {
      setSuggestions(suggestionResult.value);
    } else {
      setSuggestions([]);
      setSuggestionError(
        suggestionResult.reason instanceof Error
          ? suggestionResult.reason.message
          : "Smart suggestions are unavailable."
      );
    }

    setLoading(false);
  }

  useEffect(() => {
    void load(date);
  }, [date]);

  async function handleGenerate() {
    setGenerating(true);
    setError(null);

    try {
      const generated = await generatePlan(date);
      setPlan(generated);

      try {
        setSuggestions(await getSmartSuggestions(date));
        setSuggestionError(null);
      } catch {
        // The generated plan remains useful even if refresh of suggestions fails.
      }
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not generate a draft plan."
      );
    } finally {
      setGenerating(false);
    }
  }

  async function handleArchive() {
    if (!plan) return;
    if (!window.confirm("Clear this draft meal sketch? Your meal log will not change.")) {
      return;
    }

    try {
      await archivePlan(plan.id);
      setPlan(null);
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not clear this draft."
      );
    }
  }

  const planTotals = useMemo(() => {
    const totals = new Map<string, { amount: number; unit: string }>();

    for (const item of plan?.items ?? []) {
      for (const nutrient of item.nutrients) {
        const current = totals.get(nutrient.code);
        totals.set(nutrient.code, {
          amount: (current?.amount ?? 0) + nutrient.amount,
          unit: nutrient.unit
        });
      }
    }

    return {
      energy: totals.get("ENERGY_KCAL")?.amount ?? 0,
      protein: totals.get("PROTEIN_G")?.amount ?? 0,
      fibre: totals.get("FIBRE_G")?.amount ?? 0
    };
  }, [plan]);

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Plans"
        title="A draft meal sketch you can question and change."
        description="Aarogya ranks only source-referenced foods that pass your recorded diet and allergy filters. Regional familiarity can refine ordering, but never eligibility or clinical safety."
        action={
          <Link className="button button--secondary" href="/guidance">
            Review guidance
          </Link>
        }
      />

      <section className="planControlBar">
        <label>
          <span>Plan date</span>
          <input
            type="date"
            value={date}
            min={yesterdayKey()}
            max={futureKey(14)}
            onChange={(event) => setDate(event.target.value)}
          />
        </label>

        <div className="planControlBar__actions">
          {plan ? (
            <button className="button button--ghost" type="button" onClick={() => void handleArchive()}>
              Clear draft
            </button>
          ) : null}
          <button
            className="button button--primary"
            type="button"
            disabled={generating}
            onClick={() => void handleGenerate()}
          >
            {generating ? "Building draft…" : plan ? "Regenerate draft" : "Generate draft"}
          </button>
        </div>
      </section>

      {error ? <div className="formNotice formNotice--error" role="alert">{error}</div> : null}

      {loading ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">A</div>
          <p>Preparing planning context…</p>
        </div>
      ) : (
        <div className="planWorkspace">
          <section className="planMain">
            {plan ? (
              <>
                <section className="planSummary">
                  <div>
                    <span>Draft mode</span>
                    <strong>{pretty(plan.generationMode)}</strong>
                  </div>
                  <div>
                    <span>Guidance state</span>
                    <strong>{pretty(plan.engineStatus)}</strong>
                  </div>
                  <div>
                    <span>Source rule</span>
                    <strong>
                      {plan.sourceRuleCode
                        ? `${plan.sourceRuleCode} · v${plan.sourceRuleVersion}`
                        : "Balanced catalog variety"}
                    </strong>
                  </div>
                  <div>
                    <span>Example total</span>
                    <strong>{formatNumber(planTotals.energy)} kcal</strong>
                  </div>
                </section>

                <div className="planMealList">
                  {plan.items.map((item) => (
                    <PlanMealCard key={item.id} item={item} />
                  ))}
                </div>

                <section className="planTotalsNote">
                  <div>
                    <span className="cardEyebrow">Draft totals, not targets</span>
                    <h2>These numbers describe the example portions above.</h2>
                  </div>
                  <dl>
                    <div>
                      <dt>Energy</dt>
                      <dd>{formatNumber(planTotals.energy)} kcal</dd>
                    </div>
                    <div>
                      <dt>Protein</dt>
                      <dd>{formatNumber(planTotals.protein)} g</dd>
                    </div>
                    <div>
                      <dt>Fibre</dt>
                      <dd>{formatNumber(planTotals.fibre)} g</dd>
                    </div>
                  </dl>
                </section>

                <section className="planNotices">
                  {plan.notices.map((notice) => (
                    <p key={notice}>{notice}</p>
                  ))}
                </section>
              </>
            ) : (
              <section className="planEmpty">
                <span className="cardEyebrow">No draft yet</span>
                <h2>Start with suggestions, or ask Aarogya for a four-slot meal sketch.</h2>
                <p>
                  Generating a draft does not add anything to your meal history. It creates a separate planning artifact you can inspect or discard.
                </p>
                <button className="button button--primary" type="button" onClick={() => void handleGenerate()}>
                  Generate draft
                </button>
              </section>
            )}
          </section>

          <aside className="smartSuggestionPanel">
            <div className="smartSuggestionPanel__heading">
              <div>
                <span className="cardEyebrow">Smart suggestions</span>
                <h2>Flexible food options</h2>
              </div>
              <StatusChip tone={suggestions.length > 0 ? "positive" : "warm"}>
                {suggestions.length > 0 ? `${suggestions.length} eligible` : "Limited"}
              </StatusChip>
            </div>

            {suggestionError ? (
              <div className="nutritionPending">{suggestionError}</div>
            ) : suggestions.length > 0 ? (
              <div className="smartSuggestionList">
                {suggestions.map((suggestion) => (
                  <SuggestionCard key={suggestion.foodSlug} suggestion={suggestion} />
                ))}
              </div>
            ) : (
              <p className="smartSuggestionPanel__empty">
                No compatible source-referenced suggestions are available for the current profile.
              </p>
            )}

            <div className="smartSuggestionPanel__links">
              <Link className="quietLink smartSuggestionPanel__link" href="/foods">
                Inspect the food library
              </Link>
              <Link className="quietLink smartSuggestionPanel__link" href="/regional">
                Explore regional context
              </Link>
            </div>
          </aside>
        </div>
      )}
    </main>
  );
}

function PlanMealCard({ item }: { item: DietPlanItem }) {
  const energy = nutrient(item, "ENERGY_KCAL");
  const protein = nutrient(item, "PROTEIN_G");
  const fibre = nutrient(item, "FIBRE_G");

  return (
    <article className="planMealCard">
      <div className="planMealCard__meal">
        <span className="cardEyebrow">{mealLabels[item.mealType] ?? pretty(item.mealType)}</span>
        <h2>{item.foodName}</h2>
        <p>
          {item.portionLabel ?? "Custom portion"} · {formatNumber(item.quantityGrams)}g
        </p>
        {item.regionalFitLabel ? (
          <span className="regionalFitMini">
            {item.regionalFitLabel}
            {item.regionalFitScore !== null ? ` · ${item.regionalFitScore}/100` : ""}
          </span>
        ) : null}
      </div>

      <div className="planMealCard__metrics">
        <div>
          <span>Energy</span>
          <strong>{formatNumber(energy)} kcal</strong>
        </div>
        <div>
          <span>Protein</span>
          <strong>{formatNumber(protein)} g</strong>
        </div>
        <div>
          <span>Fibre</span>
          <strong>{formatNumber(fibre)} g</strong>
        </div>
      </div>

      <div className="planMealCard__why">
        <span>Why this appeared</span>
        <p>{item.explanation}</p>
        <small>
          {item.reasonCode.replaceAll("_", " ").toLowerCase()}
          {item.sourceCode ? ` · ${item.sourceCode}` : ""}
          {item.sourceFoodRef ? ` · ${item.sourceFoodRef}` : ""}
        </small>
      </div>

      <Link className="quietLink" href="/meals">
        Log separately if you eat this
      </Link>
    </article>
  );
}

function SuggestionCard({
  suggestion
}: {
  suggestion: SmartFoodSuggestion;
}) {
  return (
    <article className="smartSuggestion">
      <div>
        <strong>{suggestion.foodName}</strong>
        <span>
          {pretty(suggestion.category)}
          {suggestion.primaryRegion ? ` · ${suggestion.primaryRegion}` : ""}
        </span>
      </div>

      {suggestion.nutrientFocusCode && suggestion.focusNutrientAmount !== null ? (
        <div className="smartSuggestion__focus">
          <span>{nutrientLabel(suggestion.nutrientFocusCode)}</span>
          <strong>
            {formatNumber(suggestion.focusNutrientAmount)} {suggestion.focusNutrientUnit}
          </strong>
          <small>{suggestion.portionLabel}</small>
        </div>
      ) : (
        <div className="smartSuggestion__focus">
          <span>Example portion</span>
          <strong>{suggestion.portionLabel}</strong>
          <small>{formatNumber(suggestion.quantityGrams)}g</small>
        </div>
      )}

      {suggestion.regionalFitLabel ? (
        <div className="smartSuggestion__regional">
          <span>{suggestion.regionalFitLabel}</span>
          {suggestion.regionalFitScore !== null ? (
            <strong>{suggestion.regionalFitScore}/100</strong>
          ) : null}
        </div>
      ) : null}

      <p>{suggestion.explanation}</p>
    </article>
  );
}

function nutrient(item: DietPlanItem, code: string) {
  return item.nutrients.find((value) => value.code === code)?.amount ?? 0;
}

function nutrientLabel(code: string) {
  if (code === "FIBRE_G") return "Fibre";
  if (code === "PROTEIN_G") return "Protein";
  return pretty(code);
}

function pretty(value: string) {
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

function dateKey(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function todayKey() {
  return dateKey(new Date());
}

function yesterdayKey() {
  const date = new Date();
  date.setDate(date.getDate() - 1);
  return dateKey(date);
}

function futureKey(days: number) {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return dateKey(date);
}

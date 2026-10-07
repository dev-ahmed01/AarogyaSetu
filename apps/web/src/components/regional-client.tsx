"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import {
  getRegionalContext,
  getRegionalFoods,
  type RegionalContext,
  type RegionalFood
} from "@/lib/regional";

export function RegionalClient() {
  const [context, setContext] = useState<RegionalContext | null>(null);
  const [foods, setFoods] = useState<RegionalFood[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([getRegionalContext(), getRegionalFoods(12)])
      .then(([contextResult, foodResult]) => {
        setContext(contextResult);
        setFoods(foodResult);
      })
      .catch((cause) =>
        setError(
          cause instanceof Error
            ? cause.message
            : "Could not load regional food context."
        )
      )
      .finally(() => setLoading(false));
  }, []);

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Regional food context"
        title="Familiar foods, without turning culture into a rule."
        description="Aarogya uses your self-reported state or region only as a small familiarity signal. Allergy, dietary compatibility and source-backed nutrition remain higher-priority filters."
        action={
          <Link className="button button--secondary" href="/profile">
            Review region
          </Link>
        }
      />

      {error ? (
        <div className="formNotice formNotice--error" role="alert">
          {error}
        </div>
      ) : null}

      {loading ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">A</div>
          <p>Preparing regional context…</p>
        </div>
      ) : context ? (
        <>
          <section className="regionalContextCard">
            <div>
              <span className="cardEyebrow">Profile basis</span>
              <h2>
                {context.stateLabel
                  ?? context.macroRegionLabel
                  ?? "No regional profile selected"}
              </h2>
              <p>{context.disclaimer}</p>
            </div>

            <dl>
              <div>
                <dt>Broad region</dt>
                <dd>{context.macroRegionLabel ?? "All India fallback"}</dd>
              </div>
              <div>
                <dt>Source</dt>
                <dd>{pretty(context.basis)}</dd>
              </div>
              <div>
                <dt>Precise location</dt>
                <dd>Not used</dd>
              </div>
            </dl>
          </section>

          <section className="regionalSection">
            <div className="regionalSection__heading">
              <div>
                <span className="cardEyebrow">Regional fit</span>
                <h2>Foods and dishes with familiar context</h2>
              </div>
              <span>{foods.length} visible</span>
            </div>

            {foods.length > 0 ? (
              <div className="regionalFoodGrid">
                {foods.map((item) => (
                  <article className="regionalFoodCard" key={item.food.slug}>
                    <div className="regionalFoodCard__top">
                      <div>
                        <strong>{item.food.name}</strong>
                        <span>
                          {pretty(item.food.category)} · {item.fitLabel}
                        </span>
                      </div>
                      <StatusChip tone={item.planningEligible ? "positive" : "warm"}>
                        {item.planningEligible
                          ? "Planning eligible"
                          : "Discovery only"}
                      </StatusChip>
                    </div>

                    {item.localizedAliases.length > 0 ? (
                      <div className="regionalAliases">
                        {item.localizedAliases.map((alias) => (
                          <span key={`${alias.locale}:${alias.alias}`}>
                            {alias.alias}
                          </span>
                        ))}
                      </div>
                    ) : null}

                    <p>{item.rationale}</p>

                    <div className="regionalFoodCard__footer">
                      <span>Fit {item.fitScore}/100</span>
                      <span>{item.sourceCode}</span>
                    </div>
                  </article>
                ))}
              </div>
            ) : (
              <div className="regionalEmpty">
                No region-compatible foods are available for the current profile filters.
              </div>
            )}
          </section>

          <section className="regionalBoundary">
            <div>
              <span className="cardEyebrow">How ranking works</span>
              <h2>Regional fit cannot make an unsafe or unverified food eligible.</h2>
            </div>
            <p>
              Foods must first pass active/source state, dietary-pattern and allergen checks. Regional familiarity then contributes only a small ordering bonus. Discovery dishes can appear here even when nutrient curation is pending, but they are clearly kept out of meal-plan generation.
            </p>
          </section>
        </>
      ) : null}
    </main>
  );
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

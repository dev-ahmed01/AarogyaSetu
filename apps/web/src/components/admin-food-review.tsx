"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";

import { StatusChip } from "@/components/ui";
import {
  markFoodReady,
  publishFood,
  returnFoodForChanges,
  saveFoodCuration,
  unpublishFood,
  type AdminFoodDetail,
  type AdminSource
} from "@/lib/admin";

const CORE_NUTRIENTS = [
  { code: "ENERGY_KCAL", label: "Energy", unit: "kcal" },
  { code: "PROTEIN_G", label: "Protein", unit: "g" },
  { code: "CARBOHYDRATE_G", label: "Carbohydrate", unit: "g" },
  { code: "FAT_G", label: "Fat", unit: "g" },
  { code: "FIBRE_G", label: "Fibre", unit: "g" }
] as const;

type Props = {
  admin: boolean;
  detail: AdminFoodDetail;
  sources: AdminSource[];
  working: boolean;
  setWorking: (value: boolean) => void;
  onUpdated: (detail: AdminFoodDetail, notice: string) => Promise<void>;
  onError: (message: string) => void;
};

export function AdminFoodReview({
  admin,
  detail,
  sources,
  working,
  setWorking,
  onUpdated,
  onError
}: Props) {
  const [sourceId, setSourceId] = useState("");
  const [sourceFoodRef, setSourceFoodRef] = useState("");
  const [portionLabel, setPortionLabel] = useState("");
  const [portionGrams, setPortionGrams] = useState("");
  const [reviewNote, setReviewNote] = useState("");
  const [nutrients, setNutrients] =
    useState<Record<string, string>>({});

  const compositionSources = useMemo(
    () =>
      sources.filter(
        (source) => source.sourceType === "FOOD_COMPOSITION"
      ),
    [sources]
  );

  useEffect(() => {
    setSourceId(detail.source?.id ?? "");
    setSourceFoodRef(detail.sourceFoodRef ?? "");

    const portion = detail.portions.find(
      (item) => item.defaultPortion
    );
    setPortionLabel(portion?.label ?? "");
    setPortionGrams(
      portion?.grams === undefined ? "" : String(portion.grams)
    );

    const next: Record<string, string> = {};
    for (const nutrient of CORE_NUTRIENTS) {
      const current = detail.nutrients.find(
        (item) => item.code === nutrient.code
      );
      next[nutrient.code] =
        current?.amountPer100g === undefined
          ? ""
          : String(current.amountPer100g);
    }
    setNutrients(next);
    setReviewNote("");
  }, [detail]);

  const published =
    detail.summary.curationStatus === "PUBLISHED";

  async function save(event: FormEvent) {
    event.preventDefault();
    setWorking(true);

    try {
      const updated = await saveFoodCuration(
        detail.summary.id,
        {
          sourceId,
          sourceFoodRef,
          nutrients: CORE_NUTRIENTS.map((nutrient) => ({
            code: nutrient.code,
            amountPer100g: Number(nutrients[nutrient.code]),
            unit: nutrient.unit
          })),
          defaultPortionLabel: portionLabel,
          defaultPortionGrams: Number(portionGrams),
          note: reviewNote
        }
      );

      await onUpdated(
        updated,
        "Curation saved. The food remains offline until review and admin publication."
      );
    } catch (cause) {
      onError(messageOf(cause));
    } finally {
      setWorking(false);
    }
  }

  async function action(
    name: "ready" | "publish" | "unpublish" | "return"
  ) {
    setWorking(true);

    try {
      let updated: AdminFoodDetail;

      if (name === "ready") {
        updated = await markFoodReady(
          detail.summary.id,
          reviewNote
        );
      } else if (name === "publish") {
        updated = await publishFood(
          detail.summary.id,
          reviewNote
        );
      } else if (name === "unpublish") {
        updated = await unpublishFood(
          detail.summary.id,
          reviewNote
        );
      } else {
        updated = await returnFoodForChanges(
          detail.summary.id,
          reviewNote
        );
      }

      await onUpdated(updated, actionNotice(name));
    } catch (cause) {
      onError(messageOf(cause));
    } finally {
      setWorking(false);
    }
  }

  return (
    <>
      <div className="adminFoodDetail__header">
        <div>
          <span className="cardEyebrow">
            {detail.summary.slug}
          </span>
          <h2>{detail.summary.name}</h2>
          <p>
            {(detail.summary.primaryRegion ?? "No region")
              + " · "
              + detail.category
              + " · "
              + pretty(detail.dietaryClassification)}
          </p>
        </div>

        <div className="adminFoodDetail__status">
          <StatusChip
            tone={statusTone(detail.summary.curationStatus)}
          >
            {pretty(detail.summary.curationStatus)}
          </StatusChip>
          <span>
            {detail.summary.publishReady
              ? "Publish-ready data"
              : "Requirements incomplete"}
          </span>
        </div>
      </div>

      <div className="adminIntegrityRow">
        <Integrity
          label="Source"
          value={detail.summary.sourceCode ?? "Not attached"}
        />
        <Integrity
          label="Nutrients"
          value={String(detail.summary.nutrientCount)}
        />
        <Integrity
          label="Portions"
          value={String(detail.summary.portionCount)}
        />
        <Integrity
          label="Public"
          value={detail.summary.active ? "Yes" : "No"}
        />
      </div>

      {published ? (
        <div className="adminPublishedBoundary">
          <strong>
            Published content is locked for curation.
          </strong>
          <p>
            An admin must unpublish this food before nutrition
            values or provenance can be replaced. Historical
            meal and plan snapshots are not rewritten.
          </p>
        </div>
      ) : (
        <form
          className="adminCurationForm"
          onSubmit={(event) => void save(event)}
        >
          <div className="adminFormGrid">
            <label>
              <span>Food-composition source</span>
              <select
                required
                value={sourceId}
                onChange={(event) =>
                  setSourceId(event.target.value)
                }
              >
                <option value="">Choose source</option>
                {compositionSources.map((source) => (
                  <option
                    key={source.id}
                    value={source.id}
                  >
                    {source.name}
                  </option>
                ))}
              </select>
            </label>

            <label>
              <span>Source food / recipe reference</span>
              <input
                required
                value={sourceFoodRef}
                onChange={(event) =>
                  setSourceFoodRef(event.target.value)
                }
                placeholder="Dataset or recipe record ID"
              />
            </label>

            <label>
              <span>Default portion</span>
              <input
                required
                value={portionLabel}
                onChange={(event) =>
                  setPortionLabel(event.target.value)
                }
                placeholder="1 serving"
              />
            </label>

            <label>
              <span>Portion grams</span>
              <input
                required
                min="1"
                max="5000"
                step="0.01"
                type="number"
                value={portionGrams}
                onChange={(event) =>
                  setPortionGrams(event.target.value)
                }
              />
            </label>
          </div>

          <div className="adminNutrientGrid">
            {CORE_NUTRIENTS.map((nutrient) => (
              <label key={nutrient.code}>
                <span>
                  {nutrient.label + " / 100g"}
                </span>
                <div>
                  <input
                    required
                    min="0"
                    step="0.0001"
                    type="number"
                    value={nutrients[nutrient.code] ?? ""}
                    onChange={(event) =>
                      setNutrients({
                        ...nutrients,
                        [nutrient.code]:
                          event.target.value
                      })
                    }
                  />
                  <b>{nutrient.unit}</b>
                </div>
              </label>
            ))}
          </div>

          <label className="adminReviewNote">
            <span>Review note</span>
            <textarea
              maxLength={1000}
              rows={3}
              value={reviewNote}
              onChange={(event) =>
                setReviewNote(event.target.value)
              }
              placeholder="What was checked or changed?"
            />
          </label>

          <button
            className="button button--primary"
            disabled={working}
            type="submit"
          >
            Save curated nutrition
          </button>
        </form>
      )}

      <div className="adminDecisionBar">
        {!published
        && detail.summary.publishReady
        && detail.summary.curationStatus
          !== "READY_TO_PUBLISH" ? (
          <button
            className="button button--secondary"
            disabled={working}
            type="button"
            onClick={() => void action("ready")}
          >
            Mark ready
          </button>
        ) : null}

        {admin
        && (detail.summary.curationStatus
          === "READY_TO_PUBLISH"
          || detail.summary.curationStatus
          === "UNPUBLISHED") ? (
          <button
            className="button button--primary"
            disabled={working}
            type="button"
            onClick={() => void action("publish")}
          >
            Publish food
          </button>
        ) : null}

        {admin && published ? (
          <button
            className="button button--secondary"
            disabled={working}
            type="button"
            onClick={() => void action("unpublish")}
          >
            Unpublish
          </button>
        ) : null}

        {admin
        && !published
        && detail.summary.curationStatus
          !== "NEEDS_REVIEW" ? (
          <button
            className="button button--ghost"
            disabled={working || !reviewNote.trim()}
            type="button"
            onClick={() => void action("return")}
          >
            Return for changes
          </button>
        ) : null}
      </div>

      <section className="adminReviewHistory">
        <div className="adminSectionHeading">
          <div>
            <span className="cardEyebrow">
              Review history
            </span>
            <h3>Append-only decisions</h3>
          </div>
          <span>
            {detail.reviews.length + " events"}
          </span>
        </div>

        {detail.reviews.length === 0 ? (
          <p className="adminReviewHistory__empty">
            No staff review events have been recorded yet.
          </p>
        ) : (
          detail.reviews.slice(0, 8).map((review) => (
            <article key={review.id}>
              <div>
                <strong>{pretty(review.action)}</strong>
                <span>
                  {review.reviewerName
                    + " · "
                    + pretty(review.reviewerRole)}
                </span>
              </div>
              <time>
                {formatDateTime(review.occurredAt)}
              </time>
              <p>
                {pretty(review.fromStatus)
                  + " → "
                  + pretty(review.toStatus)
                  + (review.note
                    ? " · " + review.note
                    : "")}
              </p>
            </article>
          ))
        )}
      </section>
    </>
  );
}

function Integrity({
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

function statusTone(
  status: string
): "neutral" | "positive" | "warm" {
  if (
    status === "PUBLISHED"
    || status === "READY_TO_PUBLISH"
  ) {
    return "positive";
  }

  if (
    status === "NEEDS_REVIEW"
    || status === "IN_REVIEW"
  ) {
    return "warm";
  }

  return "neutral";
}

function actionNotice(action: string) {
  if (action === "ready") {
    return "Food marked ready for admin publication.";
  }

  if (action === "publish") {
    return "Food published and available to eligible product surfaces.";
  }

  if (action === "unpublish") {
    return "Food unpublished. Historical snapshots remain unchanged.";
  }

  return "Food returned for changes.";
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) =>
      letter.toUpperCase()
    );
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

function messageOf(cause: unknown) {
  return cause instanceof Error
    ? cause.message
    : "The operations request could not be completed.";
}

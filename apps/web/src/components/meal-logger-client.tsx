"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";

import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import {
  addFavorite,
  createMealEntry,
  deleteMealEntry,
  getFavoriteFoods,
  getMealDay,
  getRecentFoods,
  removeFavorite,
  updateMealEntry,
  type DailyMealLog,
  type MealEntry
} from "@/lib/meals";
import {
  getFood,
  searchFoods,
  type FoodDetail,
  type FoodSummary
} from "@/lib/nutrition";

const mealTypes = [
  ["BREAKFAST", "Breakfast"],
  ["LUNCH", "Lunch"],
  ["DINNER", "Dinner"],
  ["SNACK", "Snacks"]
] as const;

const totalOrder = ["ENERGY_KCAL", "PROTEIN_G", "FIBRE_G", "CARBOHYDRATE_G"];

const totalLabels: Record<string, string> = {
  ENERGY_KCAL: "Energy",
  PROTEIN_G: "Protein",
  FIBRE_G: "Fibre",
  CARBOHYDRATE_G: "Carbs",
  FAT_G: "Fat"
};

export function MealLoggerClient() {
  const [date, setDate] = useState(todayKey());
  const [day, setDay] = useState<DailyMealLog | null>(null);
  const [favorites, setFavorites] = useState<FoodSummary[]>([]);
  const [recent, setRecent] = useState<FoodSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [composerMeal, setComposerMeal] = useState<MealEntry["mealType"] | null>(null);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [selectedFood, setSelectedFood] = useState<FoodDetail | null>(null);
  const [search, setSearch] = useState("");
  const [searchResults, setSearchResults] = useState<FoodSummary[]>([]);
  const [portionId, setPortionId] = useState("CUSTOM");
  const [portionCount, setPortionCount] = useState("1");
  const [customGrams, setCustomGrams] = useState("100");
  const [saving, setSaving] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function refreshDay(target = date) {
    setLoading(true);
    setError(null);

    try {
      setDay(await getMealDay(target));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not load this day.");
    } finally {
      setLoading(false);
    }
  }

  async function refreshQuickFoods() {
    const [favoriteFoods, recentFoods] = await Promise.all([
      getFavoriteFoods(),
      getRecentFoods()
    ]);
    setFavorites(favoriteFoods);
    setRecent(recentFoods);
  }

  useEffect(() => {
    void refreshDay(date);
  }, [date]);

  useEffect(() => {
    refreshQuickFoods().catch(() => {
      // The primary day log remains usable if quick lists fail.
    });
  }, []);

  useEffect(() => {
    if (!composerMeal || selectedFood) return;

    let cancelled = false;
    const timer = window.setTimeout(() => {
      searchFoods({
        q: search || undefined,
        nutrientStatus: "SOURCE_REFERENCED",
        size: 8
      })
        .then((result) => {
          if (!cancelled) setSearchResults(result.foods);
        })
        .catch((cause) => {
          if (!cancelled) {
            setError(cause instanceof Error ? cause.message : "Could not search foods.");
          }
        });
    }, 220);

    return () => {
      cancelled = true;
      window.clearTimeout(timer);
    };
  }, [composerMeal, search, selectedFood]);

  const grouped = useMemo(() => {
    const result: Record<string, MealEntry[]> = {
      BREAKFAST: [],
      LUNCH: [],
      DINNER: [],
      SNACK: []
    };

    for (const entry of day?.entries ?? []) {
      result[entry.mealType].push(entry);
    }

    return result;
  }, [day]);

  const totals = useMemo(() => {
    const map = new Map((day?.totals ?? []).map((item) => [item.code, item]));
    return totalOrder.map((code) => ({
      code,
      value: map.get(code)?.amount ?? 0,
      unit: map.get(code)?.unit ?? (code === "ENERGY_KCAL" ? "kcal" : "g")
    }));
  }, [day]);

  const calculatedGrams = useMemo(() => {
    if (!selectedFood) return 0;

    if (portionId === "CUSTOM") {
      return Number(customGrams) || 0;
    }

    const portion = selectedFood.portions.find((item) => item.id === portionId);
    return portion ? portion.grams * (Number(portionCount) || 0) : 0;
  }, [customGrams, portionCount, portionId, selectedFood]);

  function openAdd(mealType: MealEntry["mealType"]) {
    resetComposer();
    setComposerMeal(mealType);
  }

  async function chooseFood(slug: string) {
    setDetailLoading(true);
    setError(null);

    try {
      const food = await getFood(slug);
      setSelectedFood(food);
      const defaultPortion = food.portions.find((item) => item.defaultPortion)
        ?? food.portions[0];

      if (defaultPortion) {
        setPortionId(defaultPortion.id);
        setPortionCount("1");
      } else {
        setPortionId("CUSTOM");
        setCustomGrams("100");
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not load food details.");
    } finally {
      setDetailLoading(false);
    }
  }

  async function editEntry(entry: MealEntry) {
    resetComposer();
    setComposerMeal(entry.mealType);
    setEditingId(entry.id);
    setDetailLoading(true);

    try {
      const food = await getFood(entry.foodSlug);
      setSelectedFood(food);

      if (
        entry.portionId &&
        food.portions.some((item) => item.id === entry.portionId)
      ) {
        setPortionId(entry.portionId);
        setPortionCount(String(entry.portionCount ?? 1));
      } else {
        setPortionId("CUSTOM");
        setCustomGrams(String(entry.quantityGrams));
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not edit this entry.");
    } finally {
      setDetailLoading(false);
    }
  }

  async function saveEntry() {
    if (!selectedFood || !composerMeal) return;

    const payload =
      portionId === "CUSTOM"
        ? {
            foodSlug: selectedFood.slug,
            mealDate: date,
            mealType: composerMeal,
            grams: Number(customGrams)
          }
        : {
            foodSlug: selectedFood.slug,
            mealDate: date,
            mealType: composerMeal,
            portionId,
            portionCount: Number(portionCount)
          };

    setSaving(true);
    setError(null);

    try {
      if (editingId) {
        await updateMealEntry(editingId, payload);
      } else {
        await createMealEntry(payload);
      }

      resetComposer();
      await Promise.all([refreshDay(date), refreshQuickFoods()]);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not save this meal.");
    } finally {
      setSaving(false);
    }
  }

  async function removeEntry(id: string) {
    if (!window.confirm("Remove this food from the meal log?")) return;

    setError(null);

    try {
      await deleteMealEntry(id);
      await Promise.all([refreshDay(date), refreshQuickFoods()]);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not remove this entry.");
    }
  }

  async function toggleFavorite() {
    if (!selectedFood) return;

    const isFavorite = favorites.some((food) => food.slug === selectedFood.slug);

    try {
      if (isFavorite) {
        await removeFavorite(selectedFood.slug);
      } else {
        await addFavorite(selectedFood.slug);
      }
      await refreshQuickFoods();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not update favourites.");
    }
  }

  function resetComposer() {
    setComposerMeal(null);
    setEditingId(null);
    setSelectedFood(null);
    setSearch("");
    setSearchResults([]);
    setPortionId("CUSTOM");
    setPortionCount("1");
    setCustomGrams("100");
  }

  function moveDate(offset: number) {
    const next = new Date(`${date}T12:00:00`);
    next.setDate(next.getDate() + offset);
    const nextKey = dateKey(next);

    if (nextKey <= todayKey()) {
      setDate(nextKey);
      resetComposer();
    }
  }

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Meals"
        title="Log what you ate. Keep the rest quiet."
        description="Entries use source-referenced catalog foods, and nutrient values are snapshotted when you save them so historical days remain stable."
        action={
          <Link className="button button--secondary" href="/foods">
            Browse food library
          </Link>
        }
      />

      <section className="mealDateBar">
        <button type="button" onClick={() => moveDate(-1)} aria-label="Previous day">
          ←
        </button>
        <label>
          <span>{date === todayKey() ? "Today" : "Meal history"}</span>
          <input
            type="date"
            value={date}
            max={todayKey()}
            onChange={(event) => {
              setDate(event.target.value);
              resetComposer();
            }}
          />
        </label>
        <button
          type="button"
          onClick={() => moveDate(1)}
          disabled={date >= todayKey()}
          aria-label="Next day"
        >
          →
        </button>
      </section>

      <section className="mealTotals" aria-label="Daily nutrient totals">
        {totals.map((total) => (
          <div key={total.code}>
            <span>{totalLabels[total.code]}</span>
            <strong>{formatNumber(total.value)}</strong>
            <small>{total.unit}</small>
          </div>
        ))}
      </section>

      {error ? <div className="formNotice formNotice--error" role="alert">{error}</div> : null}

      <div className="mealWorkspace">
        <section className="mealDay">
          {loading ? (
            <div className="catalogEmpty">Loading this day…</div>
          ) : (
            mealTypes.map(([code, label]) => (
              <article className="mealSlot" key={code}>
                <div className="mealSlot__header">
                  <div>
                    <span className="cardEyebrow">{label}</span>
                    <strong>
                      {grouped[code].length === 0
                        ? "Nothing logged"
                        : `${grouped[code].length} ${grouped[code].length === 1 ? "item" : "items"}`}
                    </strong>
                  </div>
                  <button
                    className="mealAddButton"
                    type="button"
                    onClick={() => openAdd(code)}
                  >
                    + Add food
                  </button>
                </div>

                {grouped[code].length > 0 ? (
                  <div className="mealEntryList">
                    {grouped[code].map((entry) => (
                      <div className="mealEntryRow" key={entry.id}>
                        <div>
                          <strong>{entry.foodName}</strong>
                          <span>
                            {formatNumber(entry.quantityGrams)}g
                            {entry.portionLabel ? ` · ${entry.portionLabel}` : ""}
                          </span>
                        </div>
                        <div className="mealEntryRow__energy">
                          <strong>{formatNumber(nutrient(entry, "ENERGY_KCAL"))}</strong>
                          <span>kcal</span>
                        </div>
                        <div className="mealEntryRow__actions">
                          <button type="button" onClick={() => void editEntry(entry)}>
                            Edit
                          </button>
                          <button type="button" onClick={() => void removeEntry(entry.id)}>
                            Remove
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="mealSlot__empty">
                    Add only what you actually ate. There is no penalty for an empty slot.
                  </p>
                )}
              </article>
            ))
          )}
        </section>

        <aside className="mealComposer">
          {!composerMeal ? (
            <div className="mealComposer__empty">
              <span className="cardEyebrow">Add food</span>
              <h2>Choose a meal slot first.</h2>
              <p>
                Keeping the add flow contextual avoids a large form competing with your daily log.
              </p>
            </div>
          ) : !selectedFood ? (
            <div className="mealComposer__search">
              <div className="mealComposer__heading">
                <div>
                  <span className="cardEyebrow">
                    {editingId ? "Edit entry" : `Add to ${pretty(composerMeal)}`}
                  </span>
                  <h2>Find a loggable food.</h2>
                </div>
                <button className="textAction" type="button" onClick={resetComposer}>
                  Cancel
                </button>
              </div>

              <label className="mealSearchField">
                <span>Search catalog</span>
                <input
                  autoFocus
                  type="search"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Banana, chana, dahi…"
                />
              </label>

              {favorites.length > 0 ? (
                <QuickFoodGroup title="Favourites" foods={favorites} onChoose={chooseFood} />
              ) : null}

              {recent.length > 0 ? (
                <QuickFoodGroup title="Recent" foods={recent} onChoose={chooseFood} />
              ) : null}

              <div className="mealSearchResults">
                <span className="mealComposer__subhead">Search results</span>
                {detailLoading ? (
                  <p>Loading…</p>
                ) : searchResults.length > 0 ? (
                  searchResults.map((food) => (
                    <button
                      type="button"
                      key={food.slug}
                      onClick={() => void chooseFood(food.slug)}
                    >
                      <span>
                        <strong>{food.name}</strong>
                        <small>{pretty(food.category)}</small>
                      </span>
                      <small>
                        {food.energyKcalPer100g !== null
                          ? `${formatNumber(food.energyKcalPer100g)} kcal / 100g`
                          : "Nutrients unavailable"}
                      </small>
                    </button>
                  ))
                ) : (
                  <p>No source-referenced foods match yet.</p>
                )}
              </div>
            </div>
          ) : (
            <div className="mealComposer__form">
              <div className="mealComposer__heading">
                <div>
                  <span className="cardEyebrow">
                    {editingId ? "Edit entry" : `Add to ${pretty(composerMeal)}`}
                  </span>
                  <h2>{selectedFood.name}</h2>
                </div>
                <button className="textAction" type="button" onClick={resetComposer}>
                  Cancel
                </button>
              </div>

              <button
                className="favoriteToggle"
                type="button"
                onClick={() => void toggleFavorite()}
              >
                {favorites.some((food) => food.slug === selectedFood.slug)
                  ? "★ Favourite"
                  : "☆ Add to favourites"}
              </button>

              <label className="formField">
                <span className="formField__label">Amount</span>
                <select
                  className="formField__control"
                  value={portionId}
                  onChange={(event) => setPortionId(event.target.value)}
                >
                  {selectedFood.portions.map((portion) => (
                    <option key={portion.id} value={portion.id}>
                      {portion.label} · {formatNumber(portion.grams)}g
                    </option>
                  ))}
                  <option value="CUSTOM">Custom grams</option>
                </select>
              </label>

              {portionId === "CUSTOM" ? (
                <label className="formField">
                  <span className="formField__label">Grams</span>
                  <input
                    className="formField__control"
                    type="number"
                    min="1"
                    max="5000"
                    step="1"
                    value={customGrams}
                    onChange={(event) => setCustomGrams(event.target.value)}
                  />
                </label>
              ) : (
                <label className="formField">
                  <span className="formField__label">Number of portions</span>
                  <input
                    className="formField__control"
                    type="number"
                    min="0.01"
                    max="50"
                    step="0.25"
                    value={portionCount}
                    onChange={(event) => setPortionCount(event.target.value)}
                  />
                </label>
              )}

              <div className="mealPreview">
                <div>
                  <span>Amount</span>
                  <strong>{formatNumber(calculatedGrams)}g</strong>
                </div>
                <div>
                  <span>Energy</span>
                  <strong>{formatNumber(scaledNutrient(selectedFood, "ENERGY_KCAL", calculatedGrams))} kcal</strong>
                </div>
                <div>
                  <span>Protein</span>
                  <strong>{formatNumber(scaledNutrient(selectedFood, "PROTEIN_G", calculatedGrams))}g</strong>
                </div>
                <div>
                  <span>Fibre</span>
                  <strong>{formatNumber(scaledNutrient(selectedFood, "FIBRE_G", calculatedGrams))}g</strong>
                </div>
              </div>

              <p className="mealSourceNote">
                Saved values are snapshotted from {selectedFood.source?.name ?? "the catalog"}{" "}
                {selectedFood.sourceFoodRef ? `(${selectedFood.sourceFoodRef})` : ""}.
              </p>

              <button
                className="button button--primary mealSaveButton"
                type="button"
                disabled={saving || calculatedGrams < 1}
                onClick={() => void saveEntry()}
              >
                {saving ? "Saving…" : editingId ? "Save changes" : "Add to meal"}
              </button>
            </div>
          )}
        </aside>
      </div>
    </main>
  );
}

function QuickFoodGroup({
  title,
  foods,
  onChoose
}: {
  title: string;
  foods: FoodSummary[];
  onChoose: (slug: string) => Promise<void>;
}) {
  return (
    <div className="quickFoodGroup">
      <span className="mealComposer__subhead">{title}</span>
      <div>
        {foods.slice(0, 6).map((food) => (
          <button type="button" key={food.slug} onClick={() => void onChoose(food.slug)}>
            {food.name}
          </button>
        ))}
      </div>
    </div>
  );
}

function nutrient(entry: MealEntry, code: string) {
  return entry.nutrients.find((item) => item.code === code)?.amount ?? 0;
}

function scaledNutrient(food: FoodDetail, code: string, grams: number) {
  const nutrient = food.nutrients.find((item) => item.code === code);
  return nutrient ? (nutrient.amountPer100g * grams) / 100 : 0;
}

function todayKey() {
  return dateKey(new Date());
}

function dateKey(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
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

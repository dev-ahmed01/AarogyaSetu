"use client";

import { useEffect, useMemo, useState } from "react";

import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import {
  getCatalogMetadata,
  getFood,
  searchFoods,
  type CatalogMetadata,
  type FoodDetail,
  type FoodPage
} from "@/lib/nutrition";

const nutrientLabels: Record<string, string> = {
  ENERGY_KCAL: "Energy",
  PROTEIN_G: "Protein",
  CARBOHYDRATE_G: "Carbohydrate",
  FAT_G: "Fat",
  FIBRE_G: "Fibre",
  IRON_MG: "Iron",
  CALCIUM_MG: "Calcium"
};

export function FoodCatalogClient() {
  const [metadata, setMetadata] = useState<CatalogMetadata | null>(null);
  const [results, setResults] = useState<FoodPage | null>(null);
  const [selected, setSelected] = useState<FoodDetail | null>(null);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("");
  const [dietary, setDietary] = useState("");
  const [region, setRegion] = useState("");
  const [status, setStatus] = useState("");
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getCatalogMetadata()
      .then(setMetadata)
      .catch((cause) =>
        setError(cause instanceof Error ? cause.message : "Could not load catalog filters.")
      );
  }, []);

  useEffect(() => {
    let cancelled = false;
    const timer = window.setTimeout(() => {
      setLoading(true);
      setError(null);

      searchFoods({
        q: query || undefined,
        category: category || undefined,
        dietary: dietary || undefined,
        region: region || undefined,
        nutrientStatus: status || undefined,
        size: 30
      })
        .then((page) => {
          if (cancelled) return;
          setResults(page);

          if (
            selected &&
            !page.foods.some((food) => food.slug === selected.slug)
          ) {
            setSelected(null);
          }
        })
        .catch((cause) => {
          if (!cancelled) {
            setError(
              cause instanceof Error ? cause.message : "Could not search foods."
            );
          }
        })
        .finally(() => {
          if (!cancelled) setLoading(false);
        });
    }, 220);

    return () => {
      cancelled = true;
      window.clearTimeout(timer);
    };
  }, [category, dietary, query, region, selected, status]);

  const verifiedCount = useMemo(
    () => results?.foods.filter((food) => food.loggable).length ?? 0,
    [results]
  );

  async function selectFood(slug: string) {
    setDetailLoading(true);
    setError(null);

    try {
      setSelected(await getFood(slug));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not load food details.");
    } finally {
      setDetailLoading(false);
    }
  }

  function resetFilters() {
    setQuery("");
    setCategory("");
    setDietary("");
    setRegion("");
    setStatus("");
    setSelected(null);
  }

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Nutrition knowledge"
        title="A food library that shows its evidence state."
        description="Search canonical foods, familiar aliases and supported local-script names without hiding whether nutrient data is verified, pending, or only editorial metadata."
      />

      <section className="catalogToolbar" aria-label="Food filters">
        <label className="catalogSearch">
          <span>Search foods or aliases</span>
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Try banana, chana, palak, ಅನ್ನ…"
          />
        </label>

        <label>
          <span>Category</span>
          <select value={category} onChange={(event) => setCategory(event.target.value)}>
            <option value="">All categories</option>
            {metadata?.categories.map((item) => (
              <option key={item} value={item}>{pretty(item)}</option>
            ))}
          </select>
        </label>

        <label>
          <span>Diet</span>
          <select value={dietary} onChange={(event) => setDietary(event.target.value)}>
            <option value="">All diets</option>
            {metadata?.dietaryClassifications.map((item) => (
              <option key={item} value={item}>{pretty(item)}</option>
            ))}
          </select>
        </label>

        <label>
          <span>Region</span>
          <select value={region} onChange={(event) => setRegion(event.target.value)}>
            <option value="">All regions</option>
            {metadata?.regions.map((item) => (
              <option key={item} value={item}>{item}</option>
            ))}
          </select>
        </label>

        <label>
          <span>Data state</span>
          <select value={status} onChange={(event) => setStatus(event.target.value)}>
            <option value="">Any state</option>
            {metadata?.nutrientStatuses.map((item) => (
              <option key={item} value={item}>{statusLabel(item)}</option>
            ))}
          </select>
        </label>
      </section>

      <div className="catalogSummary">
        <div>
          <strong>{results?.totalElements ?? 0}</strong>
          <span>catalog records</span>
        </div>
        <div>
          <strong>{verifiedCount}</strong>
          <span>verified in this view</span>
        </div>
        <button className="textAction" type="button" onClick={resetFilters}>
          Reset filters
        </button>
      </div>

      {error ? <div className="formNotice formNotice--error" role="alert">{error}</div> : null}

      <div className="catalogLayout">
        <section className="catalogResults" aria-label="Food results">
          {loading ? (
            <div className="catalogEmpty">Searching the catalog…</div>
          ) : results && results.foods.length > 0 ? (
            results.foods.map((food) => (
              <button
                className={
                  selected?.slug === food.slug
                    ? "foodResult is-selected"
                    : "foodResult"
                }
                type="button"
                key={food.slug}
                onClick={() => void selectFood(food.slug)}
              >
                <div className="foodResult__main">
                  <div className="foodResult__top">
                    <strong>{food.name}</strong>
                    <StatusChip tone={food.loggable ? "positive" : "warm"}>
                      {food.loggable ? "Source referenced" : "Curation pending"}
                    </StatusChip>
                  </div>
                  <p>
                    {pretty(food.category)}
                    {food.primaryRegion ? ` · ${food.primaryRegion}` : ""}
                    {" · "}
                    {pretty(food.dietaryClassification)}
                  </p>
                </div>

                <div className="foodResult__metric">
                  {food.energyKcalPer100g !== null ? (
                    <>
                      <strong>{formatNumber(food.energyKcalPer100g)}</strong>
                      <span>kcal / 100g</span>
                    </>
                  ) : (
                    <>
                      <strong>—</strong>
                      <span>no published value</span>
                    </>
                  )}
                </div>
              </button>
            ))
          ) : (
            <div className="catalogEmpty">
              No foods match these filters. Try a broader search.
            </div>
          )}
        </section>

        <aside className="foodDetailPanel" aria-live="polite">
          {detailLoading ? (
            <div className="catalogEmpty">Loading food details…</div>
          ) : selected ? (
            <FoodDetailView food={selected} />
          ) : (
            <div className="foodDetailPlaceholder">
              <span className="cardEyebrow">Food details</span>
              <h2>Select one result.</h2>
              <p>
                Nutrients, portions, allergens and provenance appear here without
                opening another dense screen.
              </p>
            </div>
          )}
        </aside>
      </div>

      <section className="catalogProvenance">
        <div>
          <span className="cardEyebrow">Source policy</span>
          <h2>Reference quality is part of the product.</h2>
        </div>
        <p>
          Numeric seed values are attached to source metadata. ICMR-NIN is kept
          as a guidance reference only; its copyrighted tables are not reproduced
          inside the catalog without permission.
        </p>
      </section>
    </main>
  );
}

function FoodDetailView({ food }: { food: FoodDetail }) {
  return (
    <div className="foodDetail">
      <div className="foodDetail__heading">
        <div>
          <span className="cardEyebrow">{pretty(food.foodType)}</span>
          <h2>{food.name}</h2>
        </div>
        <StatusChip tone={food.loggable ? "positive" : "warm"}>
          {food.loggable ? "Loggable" : "Not loggable yet"}
        </StatusChip>
      </div>

      <p className="foodDetail__description">{food.description}</p>

      {food.aliases.length > 0 ? (
        <div className="foodDetail__section">
          <h3>Also found as</h3>
          <div className="profileTags">
            {food.aliases.map((alias) => <span key={alias}>{alias}</span>)}
          </div>
        </div>
      ) : null}

      <div className="foodDetail__section">
        <h3>Nutrients per 100g</h3>
        {food.nutrients.length > 0 ? (
          <dl className="nutrientList">
            {food.nutrients.map((nutrient) => (
              <div key={nutrient.code}>
                <dt>{nutrientLabels[nutrient.code] ?? pretty(nutrient.code)}</dt>
                <dd>
                  {formatNumber(nutrient.amountPer100g)} {nutrient.unit}
                </dd>
              </div>
            ))}
          </dl>
        ) : (
          <div className="nutritionPending">
            No nutrient numbers are published for this record yet. This is
            intentional rather than an estimated placeholder.
          </div>
        )}
      </div>

      {food.portions.length > 0 ? (
        <div className="foodDetail__section">
          <h3>Portions</h3>
          <div className="portionList">
            {food.portions.map((portion) => (
              <div key={portion.id}>
                <span>{portion.label}</span>
                <strong>{formatNumber(portion.grams)}g</strong>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {food.ingredients.length > 0 ? (
        <div className="foodDetail__section">
          <h3>Known ingredient structure</h3>
          <div className="portionList">
            {food.ingredients.map((ingredient) => (
              <div key={ingredient.slug}>
                <span>{ingredient.name}</span>
                <strong>
                  {ingredient.quantityGrams !== null
                    ? `${formatNumber(ingredient.quantityGrams)}g`
                    : "recipe varies"}
                </strong>
              </div>
            ))}
          </div>
        </div>
      ) : null}

      {food.allergens.length > 0 ? (
        <div className="foodDetail__section">
          <h3>Allergen flags</h3>
          <div className="allergenList">
            {food.allergens.map((allergen) => (
              <span key={allergen}>{pretty(allergen)}</span>
            ))}
          </div>
        </div>
      ) : null}

      <div className="foodDetail__section foodSourceBox">
        <span>Source</span>
        <strong>{food.source?.name ?? "No external nutrient source yet"}</strong>
        <p>
          {food.sourceFoodRef ?? "Editorial discovery record"}
          {food.source?.license ? ` · ${food.source.license}` : ""}
        </p>
      </div>
    </div>
  );
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function statusLabel(value: string) {
  if (value === "SOURCE_REFERENCED") return "Source referenced";
  if (value === "PENDING_CURATED_DATA") return "Curation pending";
  return pretty(value);
}

function formatNumber(value: number) {
  return new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 2
  }).format(value);
}

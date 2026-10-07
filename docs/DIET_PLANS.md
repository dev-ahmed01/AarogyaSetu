# Diet Plans & Smart Food Suggestions

## Phase 8 objective

Phase 8 turns Phase 7 explanation reasons into safe, source-aware food suggestions and optional draft meal sketches.

It does not create a therapeutic diet.

The generation pipeline is:

```text
Profile + consent
      +
Phase 7 recommendation state
      +
Source-referenced catalog foods
      ↓
Diet + allergy filters
      ↓
Nutrient-focus / variety ranking
      ↓
Smart suggestions
      +
Optional persisted draft meal sketch
```

## Two outputs

### Smart suggestions

`GET /api/plans/suggestions`

Returns a flexible list of eligible catalog foods.

Suggestions are not persisted.

### Draft plan

`POST /api/plans/generate`

Creates a persisted planning artifact with up to four meal slots:

```text
BREAKFAST
LUNCH
DINNER
SNACK
```

Generating a draft does not create meal-log entries.

The user still decides what they actually eat and logs it separately.

## Generation modes

The generator derives its mode from the Phase 7 assessment.

### FIBRE_FOCUS

Triggered when the current Phase 7 assessment contains:

```text
FIBRE_TREND_LOW
```

Candidate foods are ranked partly by source-referenced fibre content.

### PROTEIN_FOCUS

Triggered when Phase 7 contains:

```text
PROTEIN_TREND_LOW
```

Candidate foods are ranked partly by source-referenced protein content.

### BALANCED_FOUNDATION

Used when no supported nutrient-gap rule is active.

This mode does not invent a target.

It uses simple catalog variety and meal-category affinity heuristics.

## Safety filters

Before ranking, every candidate must satisfy:

```text
active food
AND SOURCE_REFERENCED nutrient status
AND nutrient data present
AND dietary pattern compatible
AND no intersection with recorded allergy flags
```

If no food remains after filtering, generation stops.

## Dietary compatibility

Phase 8 reuses the Phase 7 compatibility policy.

Automatic Jain plan generation remains disabled because the current food ontology does not encode ingredient-level Jain constraints fully enough to make the filter trustworthy.

This limitation is explicit rather than guessed.

## Health-context boundary

Self-reported conditions do not turn into disease-specific diet plans.

For example, the generator does not create:

- renal diets,
- diabetic diets,
- pregnancy diets,
- PCOS treatment diets,
- thyroid treatment diets.

Phase 7 health-context suppressions still apply to the recommendation reason feeding Phase 8.

A draft may still contain ordinary catalog foods that pass diet/allergy filters, but the UI clearly states that the result is not a clinical plan.

## Ranking heuristics

Ranking is deterministic.

It uses:

1. meal/category affinity,
2. nutrient density when a Phase 7 nutrient reason exists,
3. category variety bonus,
4. stable alphabetical tie-breaking.

Example category affinities:

```text
Breakfast:
fruit, dairy, egg, cereal, nuts/seeds

Lunch:
pulse, cereal, vegetable

Dinner:
pulse, vegetable, cereal, egg

Snack:
fruit, dairy, nuts/seeds
```

These affinities and score weights are **Aarogya product heuristics**.

They are not nutritional guidelines and must not be presented as such.

## Regional boundary

Phase 8 carries regional metadata but does not use it for ranking.

Regional preference intelligence is intentionally deferred to Phase 11.

That keeps the current generator from pretending that a broad state/region field is sufficient to infer cultural food preference.

## Snapshot model

A persisted plan item freezes:

- canonical food identity,
- food-name snapshot,
- dietary-classification snapshot,
- portion reference,
- portion-label snapshot,
- gram quantity,
- source code/reference,
- reason code,
- nutrient focus,
- explanation,
- scaled nutrient snapshot.

Therefore a later catalog update does not silently rewrite an already generated draft.

## Draft totals

The UI can display the nutrient sum of the selected example portions.

Those values are labelled:

```text
Draft totals, not targets
```

They describe the generated example only.

Aarogya does not infer that the user should consume that exact calorie total.

## Regeneration

Only one active `DRAFT` is retained per user/date.

Generating again archives the previous draft.

Archived drafts remain in the database for provenance and future research/audit work.

## API

Authenticated routes:

```text
GET    /api/plans/day?date=YYYY-MM-DD
GET    /api/plans/suggestions?date=YYYY-MM-DD&limit=6
POST   /api/plans/generate
DELETE /api/plans/{planId}
```

Generate body:

```json
{
  "planDate": "2026-10-07"
}
```

Draft generation is limited to a short planning horizon around the current date.

## UI

The authenticated page is:

```text
/plans
```

The layout keeps two distinct concepts visible:

```text
Structured draft meal sketch
            |
            +---- Smart flexible food suggestions
```

The structured side answers:

> “What might a day look like?”

The suggestion side answers:

> “What compatible options could I consider?”

Neither side silently writes to the meal log.

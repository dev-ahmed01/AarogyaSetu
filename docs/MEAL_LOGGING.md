# Meal Logging

## Phase 6 objective

Phase 6 turns the source-aware food catalog into real user-generated nutrition history.

The meal log is deliberately designed as a historical record rather than a live view over mutable catalog data.

## Why nutrient snapshots exist

A meal entry stores:

- the canonical food reference,
- food-name snapshot,
- selected portion reference when used,
- portion label snapshot,
- quantity in grams,
- source code/reference snapshot,
- nutrient-status snapshot,
- scaled nutrient values at save time.

The nutrient snapshot is important.

If a catalog record is corrected or replaced later, an old meal should not silently change its historical energy/protein/fibre values.

```text
Catalog food
   ↓
quantity resolution
   ↓
nutrient scaling
   ↓
meal entry
   └── frozen nutrient snapshot
```

Editing an entry creates a new snapshot from the current catalog values.

## Tables

Phase 6 adds:

```text
meal_entries
meal_entry_nutrients
user_food_favorites
```

Recent foods are derived from real meal history rather than maintained as a second mutable list.

## Meal types

Supported slots:

```text
BREAKFAST
LUNCH
DINNER
SNACK
```

Empty slots are valid. The product does not punish or visually mark an empty meal as failure.

## Quantity model

A user can log either:

### Catalog portion

Example:

```text
1 cup cooked chickpeas = 164g
portion count = 1.5
resolved grams = 246g
```

or:

### Custom grams

Example:

```text
custom grams = 85g
```

The API rejects requests that provide both a portion and custom grams.

Allowed final quantity:

```text
1g — 5000g
```

Meal logs cannot be dated in the future.

## Loggable food boundary

Phase 6 accepts only catalog foods where:

```text
nutrient_status = SOURCE_REFERENCED
and
nutrient records are present
```

A regional discovery dish still marked `PENDING_CURATED_DATA` cannot enter calorie or nutrient totals.

## API

Authenticated routes:

```text
GET    /api/meals/day?date=YYYY-MM-DD
GET    /api/meals/history?from=YYYY-MM-DD&to=YYYY-MM-DD

POST   /api/meals/entries
PUT    /api/meals/entries/{entryId}
DELETE /api/meals/entries/{entryId}

GET    /api/meals/recent?limit=8
GET    /api/meals/favorites
PUT    /api/meals/favorites/{foodSlug}
DELETE /api/meals/favorites/{foodSlug}
```

History requests are capped at 31 days per request.

## Ownership

Every entry mutation is scoped by both:

```text
entry_id
+
authenticated user_id
```

A user cannot edit or delete another user's meal by guessing a UUID.

## Daily totals

Daily totals are derived from stored entry snapshots.

Phase 6 surfaces:

- energy,
- protein,
- fibre,
- carbohydrate,
- and any other nutrients present in the source data.

There are still **no personalized target judgments** in this phase.

The system records totals; Phase 7 will determine whether and how those totals should influence explainable guidance.

## UI

The authenticated `/meals` page now contains:

- day/date navigation,
- compact daily totals,
- breakfast/lunch/dinner/snack groups,
- catalog-backed search,
- favourites,
- recent foods,
- catalog portion selection,
- custom gram entry,
- pre-save nutrient preview,
- edit,
- delete.

The add/edit form stays in one contextual side panel to preserve the low-clutter visual hierarchy.

## Today dashboard

The Today dashboard may now display real logged totals.

It still explicitly refuses to generate personalized recommendation judgments until Phase 7.

# Regional Dietary Intelligence

## Phase 11 objective

Phase 11 adds cultural-familiarity metadata without allowing regional preference to override safety, dietary compatibility or nutrition provenance.

The ordering pipeline is:

```text
active/source state
      ↓
diet compatibility
      ↓
allergen filter
      ↓
nutrition / meal-fit score
      ↓
small regional familiarity bonus
```

Regional metadata is therefore a preference layer, not a health rule.

## Profile basis

Regional personalization uses only the user's self-reported `stateOrRegion` profile field.

It does not use:

- device GPS,
- precise location,
- IP-derived location,
- ethnicity,
- religion,
- inferred language identity.

Known states are mapped to a broad product grouping such as:

```text
Karnataka
  -> STATE_KARNATAKA
  -> SOUTH_INDIA
  -> ALL_INDIA
```

These macro-region groupings are product organization heuristics, not official cultural boundaries.

## Regional affinity

`food_region_affinities` stores:

- food,
- region code,
- 0-100 familiarity score,
- relationship type,
- rationale,
- source-code label.

Relationship types:

```text
STATE_FAMILIAR
REGIONAL_FAMILIAR
ALL_INDIA
```

Most food-to-region mappings are deliberately labelled:

```text
AAROGYA_REGIONAL_HEURISTIC
```

They must not be presented as ICMR-NIN or FSSAI recommendations.

## Planning weight

Regional familiarity contributes at most:

```text
12 ranking points
```

to plan generation.

All-India fallback contributes only a small fixed bonus.

This means regional familiarity can break ties or improve familiarity among otherwise suitable choices, but cannot make an ineligible food eligible.

## Regional discovery vs planning eligibility

Phase 11 keeps two states separate.

### Planning eligible

A food must still be:

```text
SOURCE_REFERENCED
AND nutrient data present
AND diet compatible
AND allergen compatible
```

### Discovery only

Regional dishes with:

```text
PENDING_CURATED_DATA
```

may appear on the regional discovery page but cannot enter generated plans.

When a profile has a recorded allergy, pending-curation discovery dishes are excluded because their ingredient/allergen coverage is not complete enough to make a personalized safety claim.

Automatic Jain regional personalization is also disabled until ingredient-level Jain constraints are represented reliably.

## Localized aliases

Phase 11 adds `food_localized_aliases`.

Initial coverage contains selected Kannada and Hindi script aliases for existing foods.

Examples include:

```text
Banana      -> ಬಾಳೆಹಣ್ಣು / केला
Rice        -> ಅನ್ನ / चावल
Lentils     -> ಬೇಳೆ / दाल
Peanuts     -> ಕಡಲೆಕಾಯಿ / मूंगफली
Milk        -> ಹಾಲು / दूध
Yogurt      -> ಮೊಸರು / दही
Egg         -> ಮೊಟ್ಟೆ / अंडा
Ragi mudde  -> ರಾಗಿ ಮುದ್ದೆ
```

Localized aliases participate in food-catalog search.

The alias table is intentionally independent of nutrient provenance.

## Regional alternatives

`regional_food_alternatives` stores discovery relationships such as a region-familiar staple or dish that a user may want to explore.

These are **not** nutritional substitutions.

Each relationship carries explanatory text and the alternative's nutrient-data state remains visible.

For example, a Karnataka-familiar discovery alternative can be shown without claiming it is nutritionally interchangeable with rice.

## External references

Phase 11 uses official Indian sources only as contextual references:

- ICMR-NIN Dietary Guidelines for Indians 2024
- ICMR-NIN Indian Food Composition Tables context
- FSSAI Eat Right India / millet regional recipe material

The application does not copy copyrighted ICMR-NIN nutrient tables into the catalog.

Regional affinity scores themselves are project-authored metadata.

## API

Authenticated routes:

```text
GET /api/regional/context
GET /api/regional/foods?limit=10
GET /api/regional/foods/{slug}/alternatives
```

## Plan snapshots

Generated plans now store:

```text
regional_context_code
regional_context_label
```

and each plan item can snapshot:

```text
regional_fit_score
regional_fit_label
regional_reason_snapshot
```

A future change to the regional heuristic therefore does not silently rewrite why an existing draft was ordered the way it was.

## UI

The regional workspace is:

```text
/regional
```

It is intentionally not another primary navigation item.

Users reach it from Profile or Plans.

The page shows:

- self-reported region basis,
- broad macro-region context,
- explicit statement that precise location is not used,
- familiar foods/dishes,
- localized aliases,
- planning-eligible vs discovery-only state,
- affinity source/rationale,
- safety boundary.

## Source distinction

There are three different source concepts in Phase 11 and they should not be conflated:

1. **Nutrition source** — supports nutrient numbers.
2. **Guidance/reference source** — provides contextual public-health material.
3. **Regional heuristic source** — project-authored familiarity metadata.

Only the first can make nutrient data source-referenced.

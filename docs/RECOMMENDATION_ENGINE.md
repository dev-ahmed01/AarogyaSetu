# Explainable Recommendation Engine

## Phase 7 objective

Phase 7 adds personalized nutrition interpretation without turning Aarogya into a diagnostic system or an opaque AI recommender.

The engine is deterministic and rule-based.

Every recommendation returned to the user includes:

- rule code,
- rule version,
- priority,
- reason code,
- safety class,
- observation,
- explanation,
- restrained consideration/action,
- observed value when numeric,
- reference value when numeric,
- reference unit,
- evidence-source metadata.

## Endpoint

Authenticated:

```text
GET /api/recommendations/today?date=YYYY-MM-DD
```

The browser sends its local calendar date explicitly so "today" is not silently determined by the API server timezone.

## Consent gate

The engine begins with profile/consent state.

```text
personalization consent off
        ↓
PERSONALIZATION_PAUSED
        ↓
no meal-history analysis
        ↓
no rule evaluation
```

This is enforced in the application service and covered by a test.

## Analysis window

Trend rules look at the seven completed calendar days before the assessment date.

Example:

```text
assessment date: 2026-10-07
analysis window: 2026-09-30 through 2026-10-06
```

The current partial day is excluded from nutrient-trend averages.

Immediate consistency/safety checks can still inspect the selected assessment day.

## Data-quality heuristic

A day contributes to trend analysis only when it contains:

- at least three food entries, and
- at least two distinct meal slots.

This is explicitly an **Aarogya product heuristic**, not an external nutrition guideline.

At least two such days are required before a nutrient trend can trigger.

This prevents a single snack or partially logged day from being interpreted as a full-day diet.

## Rule registry

Recommendation rules are persisted and versioned in:

```text
recommendation_rules
recommendation_evidence_sources
```

Phase 7 seeds:

```text
FIBRE_TREND_LOW v1
PROTEIN_TREND_LOW v1
ALLERGEN_CONFLICT v1
DIETARY_PATTERN_CONFLICT v1
```

The service always selects the latest active version of a rule code.

## Fibre rule

WHO's current healthy-diet guidance states that people older than 10 should aim for at least 25 g/day of naturally occurring dietary fibre.

Aarogya stores:

```text
reference = 25 g/day
trigger ratio = 0.80
minimum observed days = 2
```

Therefore the prototype surfaces a low-fibre trend only when the average is below 80% of the reference across at least two sufficiently logged completed days.

The 25 g reference is external evidence.

The 80% trigger ratio is an Aarogya product heuristic intended to avoid noisy borderline alerts. It is not a WHO threshold.

The action language remains non-prescriptive:

```text
Consider making a future meal include a fibre-rich food category
such as pulses, vegetables, fruit or whole grains.
```

## Protein rule

The WHO/FAO/UNU adult safe-level reference used by this prototype is:

```text
0.83 g protein / kg body weight / day
```

Aarogya uses this rule only when:

- age is at least 18,
- body weight is present,
- kidney condition is not selected,
- pregnancy/breastfeeding is not selected,
- enough completed-day meal data exists.

The target is calculated as:

```text
stored body weight × 0.83 g/kg/day
```

The same 80% product trigger heuristic is used to reduce noisy alerts.

Aarogya does not increase the target for muscle gain, prescribe therapeutic protein intake, or create disease-specific protein targets.

A muscle-gain goal changes **priority only**, not the numeric reference.

## Allergy conflict

When a meal is saved, Aarogya now snapshots:

- dietary classification,
- allergen flags,
- nutrient values,
- source reference.

Therefore a later catalog edit does not rewrite the safety interpretation of the original meal entry.

If a logged food's allergen snapshot intersects the user's recorded allergy set:

```text
ALLERGEN_CONFLICT
safety class = SAFETY_ATTENTION
priority = 0
```

The engine does not claim that an allergic reaction will occur and does not diagnose severity.

It tells the user to review the logged food and label and makes clear that Aarogya cannot assess reaction risk.

## Dietary-pattern consistency

Phase 7 can evaluate only classifications represented by the catalog taxonomy.

Current compatibility logic:

```text
VEGAN
  allows VEGAN

VEGETARIAN
  allows VEGAN, VEGETARIAN

EGGETARIAN
  allows VEGAN, VEGETARIAN, EGGETARIAN

PESCATARIAN
  allows the above plus PESCATARIAN
```

`NON_VEGETARIAN` and `OTHER` do not trigger compatibility conflicts.

### Jain limitation

The current food ontology does not model the ingredient-level constraints needed to evaluate Jain compatibility responsibly.

Therefore Jain profiles get an explicit limitation notice rather than a guessed automatic judgment.

## Health-context safety

Health contexts are self-reported.

Phase 7 uses them to **suppress unsafe generic inference**, not to generate disease-specific diets.

Examples:

```text
KIDNEY_CONDITION
→ generic protein targeting suppressed

PREGNANCY_OR_BREASTFEEDING
→ generic protein targeting suppressed
```

Any health context also produces an interpretation notice that Aarogya is not diagnosing or designing a clinical diet.

## Weight-management safety

A `WEIGHT_MANAGEMENT` goal does not cause the engine to prescribe a calorie deficit.

Energy totals can be observed, but Phase 7 does not infer a safe weight-loss calorie target.

## Status values

Examples:

```text
PROFILE_INCOMPLETE
PERSONALIZATION_PAUSED
NEED_MORE_DATA
READY
READY_WITH_LIMITS
READY_WITH_SAFETY_ATTENTION
LIMITED_DATA_WITH_SAFETY_ATTENTION
```

A safety conflict can appear even when nutrient trend data is insufficient.

## UI

The detailed explanation surface is:

```text
/guidance
```

Each recommendation uses:

```text
What Aarogya noticed
        ↓
Why it matters
        ↓
What you could consider
        ↓
Rule / version / evidence
```

The Today dashboard shows only the highest-priority current item and links to the detailed explanation.

## Phase boundary

Phase 7 does not generate a full meal plan or rank specific substitute foods.

Those belong to Phase 8, where candidate foods can be filtered through:

- dietary pattern,
- allergies,
- regional context,
- nutrient objective,
- catalog provenance,
- rule reason.

This keeps recommendation *reasoning* separate from food-plan generation.

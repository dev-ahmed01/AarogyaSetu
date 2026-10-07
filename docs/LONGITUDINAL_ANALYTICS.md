# Longitudinal Analytics

## Phase 13 objective

Phase 13 turns existing meal and health-record data into descriptive longitudinal views without inventing missing intake or clinical meaning.

The implementation is computed on demand from the existing source-of-truth records. No additional analytics fact table is introduced in Phase 13.

~~~text
immutable meal nutrient snapshots
            +
dated manual health observations
            ↓
equal-length analytics windows
            ↓
coverage / daily totals / comparisons
            ↓
descriptive insights only
~~~

## Supported windows

The API accepts 7-day and 30-day windows. Each current window is compared against the immediately preceding window of the same length.

For an as-of date of 7 October:

~~~text
Current 7-day window
1 Oct – 7 Oct

Previous 7-day window
24 Sep – 30 Sep
~~~

Unsupported window sizes are rejected.

## Missing data rule

An unlogged day is never treated as zero intake.

Daily points have two separate states:

~~~text
logged = true
    -> recorded nutrient totals may be present

logged = false
    -> nutrient value = null
~~~

Window averages are logged-day averages calculated only from days where the nutrient is present.

## Coverage

Coverage is distinct logged days divided by window days.

Current labels are:

~~~text
EMPTY
SPARSE
LIMITED
USABLE
~~~

These describe data completeness, not health quality.

## Nutrient comparison

Phase 13 compares ENERGY_KCAL, PROTEIN_G and FIBRE_G.

For each nutrient the response includes:

- current logged-day average,
- previous logged-day average,
- observed-day count in both windows,
- percentage change when comparison is valid,
- descriptive direction,
- plain-language interpretation.

Comparison requires at least two observed days in both windows.

A difference under 5% is labelled SIMILAR. The 5% boundary is a display heuristic for descriptive comparison, not a medical or dietary threshold.

The application never labels higher/lower as better/worse.

## Meal-pattern analytics

The current window reports entry counts and distinct days for BREAKFAST, LUNCH, DINNER and SNACK.

This describes logging patterns only. It does not infer that a missing meal type means the user skipped that meal.

## Health trend

Health-record analytics remain separately consent-gated.

Phase 13 currently supports one conservative series:

~~~text
BODY_WEIGHT
source_type = MANUAL
unit = kg
~~~

Constraints:

- HEALTH_RECORD_ANALYSIS consent must be granted,
- the observation must have a numeric value,
- observedAt must be present,
- only the last 365 days are considered,
- at most the latest 24 eligible observations are returned.

ABDM_MOCK observations are excluded at query level because the source filter is explicitly MANUAL.

The UI presents these points as a dated self-report timeline. It does not calculate BMI conclusions, weight-loss success, unhealthy gain/loss, clinical significance or disease risk.

## Descriptive insights

Insights are deterministic summaries of recorded evidence, for example:

~~~text
Meals were recorded on 5 of 7 days.

The current window contains 2 more logged days than the previous window.

Protein: the logged-day average is 8.2% higher than the previous 7-day window.
~~~

These are descriptions, not recommendations. Recommendation logic remains owned by the recommendation engine.

## API

Authenticated routes:

~~~text
GET /api/analytics/longitudinal?date=YYYY-MM-DD&window=7
GET /api/analytics/longitudinal?date=YYYY-MM-DD&window=30
~~~

## UI

The detailed analytics workspace is /analytics and is grouped under the existing Progress navigation item.

The hierarchy is:

~~~text
coverage summary
      ↓
daily nutrition timeline
      ↓
window comparison
      ↓
meal-pattern summary
      ↓
descriptive insights
      ↓
manual health-record series
      ↓
data-quality boundary
~~~

The charts use native application UI rather than a charting dependency.

## Relationship with Progress

/progress remains the behavioral consistency view.

/analytics owns descriptive longitudinal evidence.

That keeps these questions separate:

> Am I recording consistently enough to learn from?

and:

> What patterns exist in the data I recorded?

## Persistence decision

Phase 13 deliberately does not persist derived analytics.

Reasons:

- meal snapshots are already immutable historical facts,
- current analytics are inexpensive bounded-window calculations,
- storing derived values would introduce cache invalidation and provenance complexity,
- Phase 15 research/evaluation may require a different frozen-analysis model.

If later scale requires pre-aggregation, it should be introduced as an explicit materialization/cache layer rather than silently becoming another source of truth.

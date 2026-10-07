# Research & Evaluation Module

## Phase 15 objective

Phase 15 provides a privacy-safe evaluation layer for the academic Aarogya prototype.

The module is intended to support reproducible analysis of engagement and food-related behaviour signals. It does not claim that product exposure causes a dietary or health outcome.

The design keeps three concerns separate:

~~~text
product operation
      ↓
optional research consent
      ↓
minimal research instrumentation
      ↓
privacy-thresholded aggregate evaluation
~~~

## Research participation consent

Research participation uses the existing append-only consent system with a distinct consent purpose:

~~~text
RESEARCH_PARTICIPATION
policy = 2026-10-research-v1
~~~

This consent is independent from:

- health-profile personalization consent,
- health-record analysis consent.

A user may use Aarogya without joining research evaluation.

If a user revokes research participation:

- a new consent record is appended,
- research_feature_events for that user are deleted,
- the user is excluded from future evaluation calculations.

The consent history itself remains as an audit record.

## Instrumentation model

Phase 15 deliberately avoids detailed clickstream collection.

Allowed research feature events are:

~~~text
GUIDANCE_VIEWED
PLANS_VIEWED
PLAN_GENERATED
ALERTS_VIEWED
ANALYTICS_VIEWED
REGIONAL_VIEWED
~~~

Events are de-duplicated to:

~~~text
one user
+ one event type
+ one UTC day
= at most one research event
~~~

No free-text event metadata is stored.

The event table stores:

- internal user relation,
- event code,
- event version,
- UTC event date,
- timestamp.

The internal user relation exists only so consent and repeated activity can be evaluated correctly. It is never returned by research overview or export endpoints.

## Data inclusion rule

A participant is eligible only when their latest RESEARCH_PARTICIPATION consent record is granted.

Meal activity is included only when:

~~~text
meal_entry.created_at >= latest consent grant timestamp
~~~

Activity created before the latest consent grant is excluded.

If consent is later revoked, the participant is excluded entirely from future aggregate calculations.

## Privacy threshold

The minimum reportable cohort is:

~~~text
k = 5 participants
~~~

Metrics and segments with fewer than five eligible participants are suppressed.

Suppression returns:

~~~text
suppressed = true
value = null
~~~

rather than returning a small number.

The admin console may show the overall count of currently opted-in participants as an operational participation count, but outcome and cohort metrics remain thresholded.

## Evaluation window

Admin evaluation supports a selected period up to 90 days.

Defaults:

~~~text
last 30 calendar days
~~~

Future dates are rejected.

## Versioned metrics

Metric definitions live in:

~~~text
research_metric_definitions
~~~

Each definition includes:

- stable metric code,
- metric version,
- human-readable name,
- formula description,
- unit,
- fixed sub-window where applicable,
- minimum cohort size.

Current metrics are version 1.

### OPTED_IN_PARTICIPANTS v1

Counts accounts whose latest RESEARCH_PARTICIPATION consent is granted.

This is participation metadata, not a health outcome.

### ACTIVE_LOGGERS v1

Counts opted-in participants with at least one eligible meal-log day in the selected window after the latest consent grant.

### LOGGING_DAYS_PER_ACTIVE_PARTICIPANT v1

Formula:

~~~text
for each active participant:
    distinct eligible meal_date count

metric =
    mean(participant distinct-day counts)
~~~

The denominator contains only participants with at least one eligible meal-log day.

### PRE_POST_LOGGING_DAY_CHANGE_7D v1

For each eligible participant:

1. locate the participant's first retained feature exposure after research consent,
2. require that first exposure to occur in the selected reporting window,
3. require seven complete days after exposure inside the reporting window,
4. require the research consent grant to precede the seven-day pre window,
5. count distinct meal-log days in the seven days before exposure,
6. count distinct meal-log days in the seven days after exposure,
7. exclude the exposure day,
8. calculate post minus pre.

Aggregate:

~~~text
mean(
    post_7d_distinct_logging_days
    -
    pre_7d_distinct_logging_days
)
~~~

This is an observational within-participant association.

It must not be described as:

- treatment effect,
- causal impact,
- proof that Aarogya changed diet,
- clinical improvement.

## Feature-exposure summaries

For each event code, the research console counts distinct opted-in participants with at least one de-duplicated event in the selected evaluation window.

Any feature exposure cohort below five participants is suppressed.

## Dietary-pattern cohorts

The console can summarize the current broad dietary-pattern field among opted-in participants.

Examples include:

~~~text
VEGETARIAN
VEGAN
EGGETARIAN
NON_VEGETARIAN
PESCATARIAN
JAIN
OTHER
NOT_PROVIDED
~~~

Each dietary-pattern segment is independently suppressed when it contains fewer than five participants.

The research module does not expose:

- allergies,
- health contexts,
- weight,
- health observations,
- diagnoses,
- individual recommendations.

## Export model

ADMIN can export a CSV generated from the same aggregate evaluation response used by the UI.

The export may contain only unsuppressed rows for:

- versioned research metrics,
- feature-exposure cohorts,
- dietary-pattern cohorts.

The CSV never contains:

- user ID,
- email,
- name,
- raw meal entry,
- individual food history,
- health record,
- health context,
- exact consent timestamp,
- research event row.

Every export creates a security audit event:

~~~text
RESEARCH_AGGREGATE_EXPORT
~~~

## API

Participant endpoints:

~~~text
GET /api/research/consent
PUT /api/research/consent
POST /api/research/events
~~~

ADMIN-only evaluation endpoints:

~~~text
GET /api/admin/research/overview
GET /api/admin/research/metrics
GET /api/admin/research/export
~~~

## UI

User transparency is located in:

~~~text
/profile
→ Research participation
~~~

Admin evaluation is located at:

~~~text
/research
~~~

The research console is intentionally separate from the nutrition-content operations console.

## Interpretation rule

The Phase 15 module supports evaluating research hypotheses and product engagement patterns.

It does not convert secondary or observational product data into evidence of causality.

A responsible report should use language such as:

> Participants with a first recorded feature exposure showed an average difference of X logging days between the seven-day pre and post periods.

It should not say:

> Aarogya caused participants to log X more days.

## Future production considerations

Phase 16 may strengthen:

- retention/deletion controls,
- export authorization and rate limits,
- database-level privacy protections,
- integration tests against PostgreSQL,
- security headers and deployment policies,
- formal research-governance documentation.

Those production controls do not weaken the Phase 15 rule that individual participant data is never available through research evaluation endpoints.

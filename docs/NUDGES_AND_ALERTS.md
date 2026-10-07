# Health-aware Nudges & Alerts

## Phase 10 objective

Phase 10 materializes existing explainable guidance into a restrained alert system with user-controlled state.

It does **not** add a diagnostic engine.

The pipeline is:

```text
Phase 7 recommendation state
          +
consented manual health-record context
          ↓
versioned nudge routing
          ↓
deduplicated materialized nudge
          ↓
active / snoozed / acknowledged / dismissed / resolved
```

## Why materialize nudges

Recommendations answer:

> What does the current evidence/rule evaluation say?

Nudges answer:

> Which of those signals should remain visible to the user right now?

Keeping the two layers separate prevents presentation state such as snooze or dismissal from modifying the underlying nutrition evidence.

## Versioned nudge rules

Phase 10 seeds:

```text
ALLERGEN_CONFLICT_NUDGE
DIETARY_PATTERN_REVIEW
FIBRE_TREND_NUDGE
PROTEIN_TREND_NUDGE
PROFILE_WEIGHT_REVIEW
```

Every rule has:

- rule code,
- rule version,
- category,
- severity,
- cooldown duration,
- active state.

Categories:

```text
SAFETY
NUTRITION
PROFILE
```

Severities:

```text
ATTENTION
STANDARD
INFO
```

The word `ATTENTION` is intentionally used instead of emergency/critical terminology. Aarogya is not an emergency triage system.

## Recommendation routing

Phase 7 signals map as follows:

```text
ALLERGEN_CONFLICT
    -> ALLERGEN_CONFLICT_NUDGE

DIETARY_PATTERN_CONFLICT
    -> DIETARY_PATTERN_REVIEW

FIBRE_TREND_LOW
    -> FIBRE_TREND_NUDGE

PROTEIN_TREND_LOW
    -> PROTEIN_TREND_NUDGE
```

The nudge snapshots:

- recommendation reason code,
- source rule/version,
- evidence label,
- evidence URL,
- explanation text,
- action destination.

The underlying recommendation remains available on `/guidance`.

## Health-record-aware signal

Phase 10 introduces one conservative health-record signal:

```text
PROFILE_WEIGHT_REVIEW
```

It is evaluated only when:

```text
profile personalization consent = granted
AND
HEALTH_RECORD_ANALYSIS consent = granted
AND
profile weight exists
AND
latest BODY_WEIGHT observation source = MANUAL
AND
observation unit = kg
```

The signal is a **data-consistency prompt**.

It does not say that the user's body weight has medically changed. It only says that a self-reported stored measurement differs from the value currently used for personalization.

The initial difference threshold is:

```text
2.0 kg
```

This threshold is an **Aarogya product heuristic**, not a clinical guideline.

Its only action is:

```text
Review profile
```

## Synthetic ABDM exclusion

The health-observation query explicitly restricts the source to:

```text
MANUAL
```

Therefore records with:

```text
source_type = ABDM_MOCK
```

cannot generate the Phase 10 health-record nudge.

This is intentional. Synthetic demo measurements must never influence personalization.

## No lab interpretation

Phase 10 does not:

- classify HbA1c as normal or abnormal,
- classify haemoglobin as normal or abnormal,
- classify blood pressure as normal or abnormal,
- infer disease from a record,
- recommend treatment from a measurement,
- escalate a synthetic demo observation.

Lab/measurement values remain visible in the Health record timeline only.

## Deduplication

Nudges use a deterministic key such as:

```text
RULE:FIBRE_TREND_NUDGE
```

Only one materialized instance exists per user and key.

When the current signal disappears, an active/snoozed instance becomes:

```text
RESOLVED
```

If the signal recurs later, the same instance can reactivate according to its cooldown state.

## User state

Supported states:

```text
ACTIVE
SNOOZED
ACKNOWLEDGED
DISMISSED
RESOLVED
```

### Snooze

The current API supports 1–168 hours.

The UI defaults to:

```text
24 hours
```

### Acknowledge

Acknowledgement says the user has seen the nudge.

It does not change the underlying recommendation or health data.

### Dismiss

Dismissal hides the nudge until the rule's cooldown allows a still-present signal to surface again.

### Resolve

Resolution is system-driven when the signal is no longer active.

## Cooldowns

Initial product heuristics:

```text
allergen conflict       24 hours
diet-pattern review      7 days
fibre trend              7 days
protein trend            7 days
profile-weight review   14 days
```

These durations are alert-fatigue controls, not medical intervals.

## Audit

Audit events are recorded for:

- nudge evaluation,
- snooze,
- acknowledge,
- dismiss.

The audit metadata contains no free-form health-record payload.

## API

Authenticated routes:

```text
POST /api/nudges/evaluate?date=YYYY-MM-DD
GET  /api/nudges?includeHistory=false
GET  /api/nudges/summary

PUT /api/nudges/{nudgeId}/snooze
PUT /api/nudges/{nudgeId}/acknowledge
PUT /api/nudges/{nudgeId}/dismiss
```

Snooze body:

```json
{
  "hours": 24
}
```

## UI

The alert inbox is:

```text
/alerts
```

Alerts are intentionally outside the primary five-item workspace navigation.

The top bar displays only:

```text
Alerts  [count]
```

when there are active items.

Today surfaces only the highest-priority active nudge and links to the alert inbox.

This keeps the Purrfect-inspired hierarchy intact:

```text
Today
    -> one attention surface

Alerts
    -> detailed state/action management

Guidance
    -> underlying evidence and explanation
```

## Phase boundary

Phase 10 is an in-app materialized alert system.

It does not yet provide:

- push notifications,
- email or SMS delivery,
- clinician escalation,
- emergency alerts,
- background schedulers,
- diagnostic lab thresholds.

Those capabilities require separate operational and clinical governance decisions.

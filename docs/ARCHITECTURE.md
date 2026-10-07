# Architecture

## Goal

AarogyaSetu is a modular monolith for an academic nutrition and preventive-health prototype. The first implementation optimizes for correctness, traceability, testability and clear module boundaries rather than premature microservices.

## Runtime topology

```text
Browser
  |
  v
Next.js web application
  |
  | HTTPS / JSON
  v
Spring Boot API
  |
  +--> PostgreSQL
  |
  +--> future Redis cache
  |
  +--> future ABDM adapter boundary (mock first)
```

## Backend module direction

The API will evolve into these bounded modules:

- identity
- profile
- nutrition
- meals
- recommendations
- plans
- health-records
- nudges
- regional-intelligence
- engagement
- analytics
- administration
- audit

Cross-module communication should use explicit application services and domain contracts. Controllers must not directly coordinate persistence across unrelated modules.

## Layering

```text
web/controller
      |
application/service
      |
domain
      |
infrastructure/repository
```

Rules:

1. HTTP concerns stay at the boundary.
2. Domain rules remain framework-light.
3. Database entities are not API contracts.
4. Validation happens at both request and domain boundaries.
5. Security decisions are server-side.
6. Recommendations must retain provenance/reason metadata.
7. Sensitive actions become auditable events.

## Data strategy

PostgreSQL is the source of truth. Flyway owns schema evolution. Production code must never depend on Hibernate auto-DDL.

The baseline schema intentionally starts small. New tables are added by feature migrations so each phase has a reviewable data-model history.

## Recommendation architecture

The recommendation module is deterministic and versioned.

```text
Profile + consent
      +
Meal snapshots
      +
Versioned rules
      +
Evidence metadata
      ↓
Recommendation engine
      ↓
Observation + reason code + safety class + evidence
```

The browser does not independently calculate personalized guidance. External dietary references and Aarogya-specific product heuristics remain explicitly separated.

## Plan-generation architecture

The plans module consumes recommendation output but does not modify recommendation rules.

```text
Recommendation assessment
        +
Profile diet/allergy context
        +
Nutrition catalog
        ↓
Safety filter
        ↓
Deterministic ranking
        ↓
Suggestions / persisted plan snapshot
```

Plan items snapshot source and nutrient values so a later catalog update does not silently rewrite the artifact.

Regional ranking is deliberately outside this module until the regional-intelligence phase.

## Nudge architecture

Nudges are a materialized presentation/state layer over already explainable signals.

```text
recommendation engine / consented manual record signal
                    ↓
             versioned nudge rule
                    ↓
          deduplicated nudge instance
                    ↓
 ACTIVE / SNOOZED / ACKNOWLEDGED / DISMISSED / RESOLVED
```

Nudge state never changes recommendation evidence.

The health-record path is separately consent-gated and currently permits only a manual body-weight/profile consistency heuristic. The observation query explicitly restricts `source_type = MANUAL`, so synthetic ABDM demo data cannot influence personalization.

Lab-value interpretation, diagnostic thresholds and emergency escalation are outside the Phase 10 architecture.

## Regional-personalization architecture

Regional intelligence is a preference layer over the nutrition and safety layers.

```text
self-reported state/region
        ↓
state / macro / all-India hierarchy
        ↓
regional affinity metadata
        ↓
small ranking bonus
```

The regional module never changes food eligibility. Source state, dietary compatibility and allergen filtering are evaluated first.

Regional affinity scores and macro-region groupings are project-authored heuristics. They are stored separately from nutrient provenance and official guidance references.

Generated plan snapshots retain the regional context and per-item reason used at generation time.

## Engagement architecture

Phase 12 treats engagement as measurement of useful data consistency, not as a score of dietary virtue.

```text
meal-log dates
      ↓
ProgressPolicy
      ├── weekly distinct logging days
      ├── current logging run
      ├── longest logging run
      └── total logging days
                ↓
       finite milestones
```

The current-run calculation anchors to yesterday when today has not yet been logged. This prevents an unfinished day from being interpreted as a broken streak.

Achievements are append-only and are never revoked because a later day was missed.

Goal state is independent from achievement state. Pausing a goal does not erase history.

The engagement module intentionally has no calorie, weight, fasting, restrictive-eating, leaderboard or points-based reward mechanics.

## Analytics architecture

Longitudinal analytics are computed read models over persisted source facts.

~~~text
immutable meal snapshots
      +
manual consented health observations
      ↓
7 / 30-day current window
      +
previous equal-length window
      ↓
coverage + daily totals + descriptive comparisons
~~~

Unlogged days remain missing data and are never converted to zero intake. Analytics do not write back into meals, recommendations, goals, nudges or health records.

Phase 13 deliberately adds no analytics fact table: the current calculations are bounded, reproducible from source records, and avoid creating a second source of truth.

Health analytics require HEALTH_RECORD_ANALYSIS consent and currently query only MANUAL BODY_WEIGHT observations in kg. Synthetic ABDM_MOCK values are excluded before calculation, and the series is not interpreted clinically.

## Administration architecture

Phase 14 adds an operational boundary around nutrition content rather than exposing generic entity CRUD.

~~~text
NUTRITIONIST
    ↓
curation queue
    ↓
provenance + core nutrient review
    ↓
READY_TO_PUBLISH
    ↓
ADMIN
    ↓
PUBLISHED
~~~

Publication state is separate from nutrient-data state.

Published food cannot be edited in place. It must first be unpublished, then curated, reviewed and republished. Saving curated nutrition makes the catalog food inactive until final publication, preventing half-reviewed values from becoming loggable.

Every review transition is written to the append-only `food_curation_reviews` table. Sensitive operational actions also create security audit events.

The server enforces publish-ready invariants:

- FOOD_COMPOSITION provenance source,
- source record/reference identifier,
- default portion,
- energy, protein, carbohydrate, fat and fibre with expected units.

Nutritionists can curate and mark ready. Only admins can publish, unpublish, return for changes, register provenance sources and inspect the operational audit.

The operations module does not provide staff access to individual health records, meal logs or personalized recommendations.

Historical meal and plan snapshots remain immutable even when a current catalog food is unpublished or re-curated.

## Health-data safety

This application is a wellness/research prototype. It does not diagnose conditions or prescribe medical treatment.

Health context may suppress generic recommendations. It must not be used to infer a disease-specific treatment plan.

## Health-record architecture

Health records use an internal normalization layer:

```text
manual entry / external adapter
            ↓
      health record envelope
            +
   structured observations
            ↓
 provenance-aware local store
```

External integration details do not leak into downstream nutrition modules.

The internal record keeps source-system/reference, interoperability resource type, verification state and provenance text. Imported records additionally keep a source-payload hash.

Health-record analysis has a separate append-only consent purpose and is not automatically implied by storing or importing a record.

## Integration boundary

ABDM concepts inform the interoperability boundary, including consent-based exchange and FHIR alignment. Real ABDM/ABHA connectivity is not assumed.

```text
AbdmHealthRecordAdapter
        ↓
MockAbdmHealthRecordAdapter
```

The current adapter is deterministic, local-only and explicitly reports `liveConnectivity = false`. Demo subject identifiers are not ABHA numbers.

A future live provider must be implemented behind the same application contract and must not replace the mock label with a live claim unless actual credentials, conformance and consent handling exist.

## Deployment direction

- Web: Vercel-compatible Next.js
- API: containerized Spring Boot
- Database: managed PostgreSQL
- CI: GitHub Actions

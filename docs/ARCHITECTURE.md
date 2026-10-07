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

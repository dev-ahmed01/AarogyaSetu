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

## Health-data safety

This application is a wellness/research prototype. It does not diagnose conditions or prescribe medical treatment.

Health context may suppress generic recommendations. It must not be used to infer a disease-specific treatment plan.

## Integration boundary

Real ABDM/ABHA connectivity is not assumed. The system will first implement an adapter interface and deterministic mock provider so demos do not misrepresent access to government health infrastructure.

## Deployment direction

- Web: Vercel-compatible Next.js
- API: containerized Spring Boot
- Database: managed PostgreSQL
- CI: GitHub Actions

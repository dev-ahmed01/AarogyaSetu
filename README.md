# AarogyaSetu — Nutrition & Preventive Health Research Prototype

> **Academic research prototype. This repository is not affiliated with, endorsed by, or operated by the Government of India, NIC, NHA, ABDM, FSSAI, ICMR, or the official Aarogya Setu application.**

AarogyaSetu is an academic software implementation inspired by the research paper **“Aarogya Setu and Food Diet Preferences: An Analysis of How the App Influences Food Choices Among Indian Users.”**

The product explores how a digital-health platform could responsibly extend into nutrition self-monitoring, personalized dietary guidance, regional food intelligence, longitudinal health trends, and explainable behaviour-change nudges.

## Phase status

**Phase 11/16 — Regional Dietary Intelligence: complete**

See [docs/ROADMAP.md](docs/ROADMAP.md) for the full delivery sequence.

## Current product surfaces

Public:
- `/` — research/product landing page
- `/login` — sign in
- `/signup` — account creation

Authenticated:
- `/onboarding` — focused health-profile setup
- `/profile` — profile and consent review
- `/dashboard` — truthful Today workspace with real meal totals and top explainable guidance
- `/foods` — source-aware food knowledge library
- `/meals` — full daily meal logging, history, favourites and recent foods
- `/guidance` — explainable personalized recommendation detail
- `/plans` — smart food suggestions and persisted draft meal sketches
- `/health` — longitudinal health-record timeline, provenance, consent and ABDM architecture demo
- `/alerts` — focused alert inbox with snooze, acknowledge, dismiss and history
- `/progress` — longitudinal progress workspace foundation

## Implemented foundation

### Authentication & security

- PostgreSQL-backed user accounts
- BCrypt password hashing
- `USER`, `NUTRITIONIST`, and `ADMIN` roles
- signed access and refresh JWTs
- HttpOnly browser cookies
- refresh-token rotation and revocation
- protected workspace session gate
- JSON 401/403 responses
- security audit-event foundation

### Health profile & consent

- age without storing full date of birth
- optional nutrition-relevant sex
- optional height and weight
- activity level
- dietary pattern
- broad state/region only
- nutrition goals
- food allergies
- optional self-reported health context
- append-only personalization consent records
- server-side prevention of profile writes before consent

### Nutrition knowledge

- canonical food identity and stable slugs
- aliases for search
- portions mapped to grams
- nutrients stored per 100g
- dietary classifications
- allergen flags
- broad region tags
- ingredient relationships for composed dishes
- source/provenance records
- explicit nutrient-data quality state
- paginated/filterable catalog API
- source-aware food library UI
- regional discovery foods that remain unloggable until curated

### Meal logging

- breakfast, lunch, dinner and snack slots
- catalog-backed food search
- catalog portions or custom gram quantities
- immutable nutrient snapshots per saved entry
- daily nutrient aggregation
- edit/delete with authenticated ownership checks
- date navigation and 31-day history API
- favourites and recent foods
- Today dashboard backed by real meal totals
- historical meal snapshots for nutrient and safety interpretation

### Explainable personalized guidance

- consent-gated recommendation evaluation
- seven completed-day analysis window
- data-quality gate before trend interpretation
- versioned recommendation rules and evidence metadata
- WHO-referenced fibre trend rule
- WHO/FAO/UNU-referenced adult protein trend rule
- allergy conflict detection from meal snapshots
- dietary-pattern consistency checks
- safety suppression for kidney and pregnancy/breastfeeding contexts
- no calorie-deficit prescription for weight-management goals
- `/guidance` explanation surface with rule/reason/evidence visibility

### Diet plans & smart food suggestions

- source-referenced candidate foods only
- diet-pattern and allergen filtering before ranking
- fibre/protein focus derived from Phase 7 reason codes
- balanced fallback without inventing a nutrient target
- deterministic meal/category affinity and variety heuristics
- persisted four-slot draft meal sketches
- food/portion/source/nutrient snapshots in each plan item
- regenerating a date archives the prior draft
- draft totals labelled as example totals, not intake targets
- Jain automatic planning explicitly disabled until ingredient-level constraints are modeled
- regional ranking deliberately deferred to Phase 11

### Health records & ABDM architecture

- longitudinal health-record envelopes with structured observations
- self-reported/manual provenance state
- external source/reference metadata
- source payload hashes for imported provenance
- append-only health-record analysis consent
- security audit events for sensitive record/integration actions
- deterministic mock ABDM/ABHA adapter
- explicit mock mode with `liveConnectivity = false`
- demo identifiers that cannot be mistaken for ABHA numbers
- duplicate-safe mock imports
- disconnect without silently deleting local copies

### Health-aware nudges & alerts

- versioned nudge-routing rules
- deterministic per-user deduplication keys
- active, snoozed, acknowledged, dismissed and resolved states
- rule-specific cooldowns for alert-fatigue control
- Phase 7 recommendation-to-nudge routing
- allergy conflicts surfaced as attention alerts
- nutrition trends surfaced as standard nudges
- separate profile/data-consistency nudges
- health-record analysis consent required before manual record data can influence a nudge
- synthetic `ABDM_MOCK` observations excluded from personalization
- no lab-value classification or diagnostic threshold engine
- top-bar active-alert count
- Today shows only the highest-priority active nudge

### Regional dietary intelligence

- self-reported state/region resolver with broad macro-region fallback
- no GPS or precise-location dependency
- project-authored regional familiarity scores kept separate from nutrition provenance
- selected Kannada and Hindi script aliases
- localized aliases participate in catalog search
- regional discovery foods remain visibly distinct from planning-eligible foods
- recorded-allergy profiles do not receive pending-curation regional dishes
- Jain regional personalization remains disabled pending ingredient-level constraints
- regional plan bonus capped at 12 points
- generated plans snapshot the regional context and per-item regional reason

## Data provenance

The prototype keeps nutrition sources explicit.

USDA FoodData Central is used for the initial public-domain nutrient seed layer.

ICMR-NIN 2024 Dietary Guidelines are registered as a guidance reference only. Their numeric tables are **not** copied into the product because the publication states that electronic-product reproduction/storage requires prior permission.

See [docs/NUTRITION_CATALOG.md](docs/NUTRITION_CATALOG.md).

## Architecture

```text
apps/web            Next.js + TypeScript frontend
services/api        Java + Spring Boot REST API
docs                Architecture, UX, security, profile and nutrition notes
```

PostgreSQL is the system of record.

Database migrations currently include:

```text
V1__baseline.sql
V2__identity_and_sessions.sql
V3__health_profiles_and_consent.sql
V4__nutrition_catalog.sql
V5__meal_logging.sql
V6__recommendation_engine.sql
V7__diet_plans.sql
V8__health_records_and_abdm.sql
V9__nudges_and_alerts.sql
V10__regional_dietary_intelligence.sql
```

## Nutrition and meal endpoints

Authenticated:

```text
GET /api/nutrition/foods
GET /api/nutrition/foods/{slug}
GET /api/nutrition/metadata

GET    /api/meals/day
GET    /api/meals/history
POST   /api/meals/entries
PUT    /api/meals/entries/{entryId}
DELETE /api/meals/entries/{entryId}
GET    /api/meals/recent
GET    /api/meals/favorites
PUT    /api/meals/favorites/{foodSlug}
DELETE /api/meals/favorites/{foodSlug}

GET /api/recommendations/today?date=YYYY-MM-DD

GET    /api/plans/day?date=YYYY-MM-DD
GET    /api/plans/suggestions?date=YYYY-MM-DD&limit=6
POST   /api/plans/generate
DELETE /api/plans/{planId}

GET    /api/health/summary
GET    /api/health/records
GET    /api/health/records/{recordId}
POST   /api/health/records
DELETE /api/health/records/{recordId}
GET    /api/health/consent
PUT    /api/health/consent

GET    /api/health/integrations/abdm/status
POST   /api/health/integrations/abdm/connect
POST   /api/health/integrations/abdm/import
DELETE /api/health/integrations/abdm/connection

POST /api/nudges/evaluate?date=YYYY-MM-DD
GET  /api/nudges?includeHistory=false
GET  /api/nudges/summary
PUT  /api/nudges/{nudgeId}/snooze
PUT  /api/nudges/{nudgeId}/acknowledge
PUT  /api/nudges/{nudgeId}/dismiss

GET /api/regional/context
GET /api/regional/foods?limit=10
GET /api/regional/foods/{slug}/alternatives
```

Meal history is snapshot-based: later catalog edits do not silently rewrite previously saved nutrient totals. Recommendation responses carry rule/version/reason/evidence metadata rather than returning an opaque score.

## Product principles

- Evidence before novelty.
- Explainable recommendations over black-box medical claims.
- Privacy and consent by design.
- Data minimization.
- Source provenance for nutrition data.
- No invented nutrient values for uncurated foods.
- Progressive disclosure instead of dense health dashboards.
- Indian and regional food context.
- Strong separation between wellness guidance and medical diagnosis.
- Accessible, responsive user experience.
- Auditable backend behaviour.

## Visual direction

The interface follows the hierarchy principles of the supplied Purrfect reference: generous whitespace, restrained warm surfaces, strong display/body typography contrast, rounded controls, clear sections and progressive disclosure.

Aarogya uses a distinct green/warm health identity and avoids dense medical-dashboard styling.

See:
- [Design system](docs/DESIGN_SYSTEM.md)
- [UX architecture](docs/UX_ARCHITECTURE.md)
- [System architecture](docs/ARCHITECTURE.md)
- [Security model](docs/SECURITY.md)
- [Profile & consent model](docs/PROFILE_AND_CONSENT.md)
- [Nutrition catalog](docs/NUTRITION_CATALOG.md)
- [Meal logging](docs/MEAL_LOGGING.md)
- [Recommendation engine](docs/RECOMMENDATION_ENGINE.md)
- [Diet plans & suggestions](docs/DIET_PLANS.md)
- [Health records & ABDM architecture](docs/HEALTH_RECORDS_ABDM.md)
- [Nudges & alerts](docs/NUDGES_AND_ALERTS.md)
- [Regional dietary intelligence](docs/REGIONAL_DIETARY_INTELLIGENCE.md)

## Local development

Prerequisites:
- Node.js 20+
- Java 21
- Maven 3.9+
- Docker / Docker Compose

### 1. Start PostgreSQL

```bash
cp .env.example .env
docker compose up -d db
```

### 2. Start the API

```bash
cd services/api
mvn spring-boot:run
```

### 3. Start the web application

```bash
cd apps/web
npm install
npm run dev
```

Default local endpoints:
- Web: http://localhost:3000
- API status: http://localhost:8080/api/status
- API health: http://localhost:8080/actuator/health

## Safety boundary

This project is a wellness/research prototype. It must not diagnose disease, prescribe treatment, or represent generated guidance as a substitute for professional medical or dietetic care.

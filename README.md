# AarogyaSetu — Nutrition & Preventive Health Research Prototype

> **Academic research prototype. This repository is not affiliated with, endorsed by, or operated by the Government of India, NIC, NHA, ABDM, FSSAI, ICMR, or the official Aarogya Setu application.**

AarogyaSetu is an academic software implementation inspired by the research paper **“Aarogya Setu and Food Diet Preferences: An Analysis of How the App Influences Food Choices Among Indian Users.”**

The product explores how a digital-health platform could responsibly extend into nutrition self-monitoring, personalized dietary guidance, regional food intelligence, longitudinal health trends, and explainable behaviour-change nudges.

## Phase status

**Phase 5/16 — Nutrition Knowledge Platform: complete**

See [docs/ROADMAP.md](docs/ROADMAP.md) for the full delivery sequence.

## Current product surfaces

Public:
- `/` — research/product landing page
- `/login` — sign in
- `/signup` — account creation

Authenticated:
- `/onboarding` — focused health-profile setup
- `/profile` — profile and consent review
- `/dashboard` — truthful Today workspace
- `/foods` — source-aware food knowledge library
- `/meals` — meal workspace foundation linked to the catalog
- `/plans` — diet-plan workspace foundation
- `/health` — health-record workspace foundation
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
```

## Nutrition endpoints

Authenticated:

```text
GET /api/nutrition/foods
GET /api/nutrition/foods/{slug}
GET /api/nutrition/metadata
```

Search supports name/alias, category, dietary classification, broad region, nutrient-data status and pagination.

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

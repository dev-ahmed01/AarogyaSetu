# AarogyaSetu — Nutrition & Preventive Health Research Prototype

> **Academic research prototype. This repository is not affiliated with, endorsed by, or operated by the Government of India, NIC, NHA, ABDM, FSSAI, ICMR, or the official Aarogya Setu application.**

AarogyaSetu is an academic software implementation inspired by the research paper **“Aarogya Setu and Food Diet Preferences: An Analysis of How the App Influences Food Choices Among Indian Users.”**

The product explores how a digital-health platform could responsibly extend into nutrition self-monitoring, personalized dietary guidance, regional food intelligence, longitudinal health trends, and explainable behaviour-change nudges.

## Phase status

**Phase 1/16 — Repository & Architecture Foundation: in progress**

## Product principles

- Evidence before novelty.
- Explainable recommendations over black-box medical claims.
- Privacy and consent by design.
- Progressive disclosure instead of dense health dashboards.
- Indian and regional food context.
- Strong separation between wellness guidance and medical diagnosis.
- Accessible, responsive user experience.
- Auditable backend behaviour.

## Architecture

```text
apps/web            Next.js + TypeScript frontend
services/api        Java + Spring Boot REST API
infra               Docker/local infrastructure
docs                Architecture, UX, API and research notes
```

PostgreSQL is the system of record. Redis is reserved for caching/rate-limit/session-adjacent use cases when the product reaches the relevant phases.

## Planned modules

1. Authentication & access control
2. Health onboarding and profile
3. Nutrition knowledge base
4. Meal logging
5. Explainable recommendation engine
6. Diet plans and food substitutions
7. Health records / mock ABDM integration boundary
8. Health-aware nudges
9. Regional dietary intelligence
10. Goals and engagement
11. Longitudinal analytics
12. Admin / nutritionist workspace
13. Research evaluation

## Visual direction

The UI follows the visual hierarchy principles of the supplied Purrfect reference: generous whitespace, restrained warm surfaces, strong typography contrast, rounded controls, clear sections and progressive disclosure. Aarogya uses a distinct health-oriented identity rather than copying the pet-travel branding.

## Local development

Prerequisites:

- Node.js 20+
- Java 21
- Maven 3.9+
- Docker / Docker Compose

### Infrastructure

```bash
cp .env.example .env
docker compose up -d db
```

### API

```bash
cd services/api
./mvnw spring-boot:run
```

### Web

```bash
cd apps/web
npm install
npm run dev
```

Default local endpoints:

- Web: http://localhost:3000
- API: http://localhost:8080
- Health: http://localhost:8080/actuator/health

## Safety boundary

This project is a wellness/research prototype. It must not diagnose disease, prescribe treatment, or represent generated guidance as a substitute for professional medical or dietetic care.

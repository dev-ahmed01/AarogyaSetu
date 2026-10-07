# AarogyaSetu — Nutrition & Preventive Health Research Prototype

> **Academic research prototype. This repository is not affiliated with, endorsed by, or operated by the Government of India, NIC, NHA, ABDM, FSSAI, ICMR, or the official Aarogya Setu application.**

AarogyaSetu is an academic software implementation inspired by the research paper **“Aarogya Setu and Food Diet Preferences: An Analysis of How the App Influences Food Choices Among Indian Users.”**

The product explores how a digital-health platform could responsibly extend into nutrition self-monitoring, personalized dietary guidance, regional food intelligence, longitudinal health trends, and explainable behaviour-change nudges.

## Phase status

**Phase 1/16 — Repository & Architecture Foundation: complete**

See [docs/ROADMAP.md](docs/ROADMAP.md) for the full delivery sequence.

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
infra               introduced as deployment needs grow
docs                Architecture, UX and research notes
```

PostgreSQL is the system of record. Redis is reserved for later phases where caching or rate limiting provides a concrete benefit.

## Visual direction

The UI follows the visual-hierarchy principles of the supplied Purrfect reference: generous whitespace, restrained warm surfaces, strong typography contrast, rounded controls, clear sections and progressive disclosure. Aarogya uses a distinct health-oriented identity rather than copying the pet-travel branding.

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

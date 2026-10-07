# AarogyaSetu — Nutrition & Preventive Health Research Prototype

> **Academic research prototype. This repository is not affiliated with, endorsed by, or operated by the Government of India, NIC, NHA, ABDM, FSSAI, ICMR, or the official Aarogya Setu application.**

AarogyaSetu is an academic software implementation inspired by the research paper **“Aarogya Setu and Food Diet Preferences: An Analysis of How the App Influences Food Choices Among Indian Users.”**

The product explores how a digital-health platform could responsibly extend into nutrition self-monitoring, personalized dietary guidance, regional food intelligence, longitudinal health trends, and explainable behaviour-change nudges.

## Phase status

**Phase 3/16 — Authentication & Security: complete**

See [docs/ROADMAP.md](docs/ROADMAP.md) for the full delivery sequence.

## Current product surfaces

Public:
- `/` — research/product landing page
- `/login` — sign in
- `/signup` — account creation

Authenticated workspace:
- `/dashboard` — Today dashboard shell
- `/meals` — meal workspace foundation
- `/plans` — diet-plan workspace foundation
- `/health` — health-record workspace foundation
- `/progress` — longitudinal progress workspace foundation

## Phase 3 security

- PostgreSQL-backed user accounts
- BCrypt password hashing
- `USER`, `NUTRITIONIST`, and `ADMIN` roles
- signed access JWTs
- signed refresh JWTs
- HttpOnly browser cookies
- persisted refresh-session hashes
- refresh-token rotation
- server-side logout/revocation
- authenticated `/api/auth/me`
- JSON 401 / 403 responses
- security audit-event foundation
- protected workspace session gate

The browser UI does **not** store authentication tokens in localStorage.

See [docs/SECURITY.md](docs/SECURITY.md) for the security model and deployment caveats.

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
docs                Architecture, UX, security and research notes
```

PostgreSQL is the system of record. Redis is reserved for later phases where caching or rate limiting provides a concrete benefit.

## Visual direction

The interface follows the hierarchy principles of the supplied Purrfect reference: generous whitespace, restrained warm surfaces, strong display/body typography contrast, rounded controls, clear sections and progressive disclosure.

Aarogya uses a distinct green/warm health identity and deliberately avoids dense medical-dashboard styling.

See:
- [Design system](docs/DESIGN_SYSTEM.md)
- [UX architecture](docs/UX_ARCHITECTURE.md)
- [System architecture](docs/ARCHITECTURE.md)
- [Security model](docs/SECURITY.md)

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

Authentication endpoints:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/auth/me`

## Safety boundary

This project is a wellness/research prototype. It must not diagnose disease, prescribe treatment, or represent generated guidance as a substitute for professional medical or dietetic care.

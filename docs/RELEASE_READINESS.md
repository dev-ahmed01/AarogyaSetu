# Release Readiness

## Release

~~~text
Aarogya v1.0.0
Phase 16/16
Academic research prototype
~~~

This release is intended for controlled academic demonstrations, research evaluation and software-engineering review.

It is not:

- the official Government of India Aarogya Setu application,
- a medical device,
- a diagnostic system,
- a treatment or prescribing system,
- a live ABDM / ABHA integration,
- a substitute for clinician or dietitian judgment.

## Release gate

A commit is release-ready only when all GitHub Actions jobs pass:

~~~text
Web checks
  ├── dependency install
  ├── TypeScript strict check
  └── Next.js production build

API + PostgreSQL checks
  ├── Java 21 build/test
  ├── PostgreSQL 16 service
  ├── Spring application context
  ├── Flyway V1 → V13
  ├── readiness probe
  └── security-header smoke test

Container build checks
  ├── docker compose config
  ├── API production image
  └── web standalone production image
~~~

## Security hardening

Phase 16 adds:

- production startup refusal for development/default secrets,
- minimum 64-character production JWT secret,
- secure-cookie requirement in production,
- explicit HTTPS-only production CORS origins,
- rejection of SameSite=None while CSRF protection is absent,
- authentication endpoint rate limiting,
- API no-store caching headers,
- clickjacking protection,
- MIME sniffing protection,
- restrictive API CSP,
- referrer and browser-permission policies,
- JWT data minimization,
- hashed unauthenticated email audit subjects,
- password-confirmed user account deletion,
- personal-data self export,
- audit anonymization during deletion,
- bounded database-pool configuration,
- graceful shutdown,
- public liveness/readiness probes,
- non-root runtime containers,
- Dependabot update monitoring.

## CSRF and deployment topology

Browser authentication uses HttpOnly cookies.

The current release intentionally keeps:

~~~text
SameSite=Lax or Strict
~~~

and does not implement a separate CSRF token protocol.

Therefore a production deployment must keep the web app and API same-site, for example:

~~~text
https://app.example.com
https://api.example.com
~~~

Do not deploy the browser on one unrelated site and the API on another while changing cookies to:

~~~text
SameSite=None
~~~

The production startup guard rejects that configuration.

If a future deployment requires true cross-site cookies, explicit CSRF protection must be implemented first.

## Authentication abuse boundary

These endpoints are rate-limited per application instance and remote address:

~~~text
POST /api/auth/login
POST /api/auth/register
POST /api/auth/refresh
~~~

Default:

~~~text
20 requests / 300 seconds
~~~

Configuration:

~~~text
AUTH_RATE_LIMIT_ATTEMPTS
AUTH_RATE_LIMIT_WINDOW_SECONDS
~~~

The built-in limiter is appropriate for the academic deployment and single-instance service.

A multi-instance public deployment should move abuse limiting to a shared gateway / reverse proxy / Redis-backed limiter.

## JWT minimization

Access tokens contain only:

- account ID as subject,
- token type,
- issued-at time,
- expiry.

Email, display name and role are not embedded.

The API reloads the current account from PostgreSQL when authenticating, so role changes and account disablement are evaluated against current server state.

Refresh tokens remain hashed in the database and rotate on use.

## Personal-data export

Authenticated users can request:

~~~text
GET /api/account/export
~~~

The export includes their own stored:

- account metadata,
- health profile,
- profile goals / allergies / health contexts,
- consent history,
- meal records and nutrient snapshots,
- allergen snapshots,
- favourites,
- diet plans and plan snapshots,
- health records and observations,
- health-integration metadata,
- nudges,
- wellness goals,
- achievements,
- research feature events,
- security events associated with the account.

It does not include:

- password hashes,
- raw refresh tokens,
- other users,
- administration records belonging to other staff.

The export itself creates a security audit event.

## Account deletion

Normal USER accounts can request:

~~~text
DELETE /api/account
~~~

with their current password.

Deletion:

1. verifies the password,
2. records the deletion request,
3. removes retained raw audit subject/metadata for the account,
4. deletes the user account,
5. relies on database cascades for user-owned records,
6. clears browser authentication cookies.

Staff accounts cannot self-delete through the product because food-curation review history uses attributable staff reviewers and an operator-controlled offboarding process is required.

## Research privacy

Research evaluation remains separately consented.

The final release keeps:

- daily de-duplicated feature events,
- no arbitrary clickstream,
- event deletion after research-consent withdrawal,
- minimum cohort size of five,
- suppressed small cells,
- aggregate-only CSV export,
- no participant drill-down,
- no causal claims from observational comparisons.

## Database safety

PostgreSQL is the only system of record.

Production configuration uses:

~~~text
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.clean-disabled=true
spring.flyway.validate-on-migrate=true
~~~

Application startup fails when entity/schema expectations do not match.

The CI integration test boots against PostgreSQL 16 and confirms migration V13 is successfully applied.

## Containers

Both application containers run as non-root users.

API image:

- Java 21 JRE,
- memory-aware JVM ceiling,
- readiness health check,
- Spring graceful shutdown.

Web image:

- Next.js standalone output,
- non-root node user,
- HTTP health check,
- no source-level development server.

## Dependency baseline

Release baseline:

~~~text
Java              21
Spring Boot       3.5.x
PostgreSQL        16
Node.js           20
Next.js           15.5.x maintained backport
React             19.1
TypeScript        5.7
~~~

Dependabot monitors:

- Maven,
- npm,
- GitHub Actions.

Dependency updates still require CI to pass before merge.

## Demo-data strategy

The database migrations seed:

- nutrition sources,
- canonical food examples,
- portions and nutrient data,
- recommendation evidence/rules,
- nudge rules,
- regional metadata,
- engagement goal templates,
- achievements,
- research metric definitions.

No default user password is committed.

Demo accounts should be created through normal registration.

ADMIN / NUTRITIONIST promotion for a local academic demo is an explicit trusted database operation documented in ADMIN_OPERATIONS.md.

## Pre-deployment checklist

Before setting APP_ENV=production:

- create a managed PostgreSQL database,
- set a unique strong database password,
- generate a unique JWT secret of at least 64 characters,
- use HTTPS,
- use same-site web/API domains,
- set COOKIE_SECURE=true,
- keep COOKIE_SAME_SITE=Lax or Strict,
- set exact HTTPS CORS origin(s),
- configure backups on the managed PostgreSQL service,
- verify readiness endpoint,
- verify the UI can register/login/log out,
- create staff users through trusted provisioning,
- verify research and health-record consent boundaries,
- run the complete CI suite.

## Remaining boundaries

These are deliberate product boundaries, not unfinished Phase 16 tasks:

- ABDM adapter remains deterministic mock-only.
- No medical diagnosis or treatment recommendations.
- No automatic interpretation of lab abnormality.
- No SMS/email infrastructure or password-reset provider.
- No MFA provider.
- No distributed rate-limiter backend.
- No cross-site CSRF-cookie mode.
- No production clinical validation.

Those capabilities require separate security, regulatory or infrastructure projects rather than being silently simulated in this repository.

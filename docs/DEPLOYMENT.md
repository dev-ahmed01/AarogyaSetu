# Deployment Guide

## Recommended topology

For cookie security, use same-site HTTPS domains:

~~~text
Browser
  ↓
https://app.example.com      Next.js
  ↓ credentialed HTTPS
https://api.example.com      Spring Boot
  ↓ private network
Managed PostgreSQL
~~~

The two hostnames share the same registrable site.

Do not set COOKIE_SAME_SITE=None for this release.

## Local full-stack startup

Create environment file:

~~~bash
cp .env.example .env
~~~

Start the whole system:

~~~bash
docker compose up --build
~~~

Default URLs:

~~~text
Web       http://localhost:3000
API       http://localhost:8080/api/status
Readiness http://localhost:8080/actuator/health/readiness
Postgres  localhost:5432
~~~

Stop:

~~~bash
docker compose down
~~~

Remove local database volume only when intentional:

~~~bash
docker compose down -v
~~~

## Production environment

Required examples:

~~~text
APP_ENV=production

DATABASE_URL=jdbc:postgresql://<private-db-host>:5432/aarogya
POSTGRES_USER=<production-user>
POSTGRES_PASSWORD=<strong-production-password>

JWT_SECRET=<unique-random-value-at-least-64-characters>
COOKIE_SECURE=true
COOKIE_SAME_SITE=Lax

CORS_ALLOWED_ORIGINS=https://app.example.com

NEXT_PUBLIC_API_BASE_URL=https://api.example.com/api
~~~

Optional tuning:

~~~text
ACCESS_TOKEN_MINUTES=15
REFRESH_TOKEN_DAYS=30

AUTH_RATE_LIMIT_ATTEMPTS=20
AUTH_RATE_LIMIT_WINDOW_SECONDS=300

DB_CONNECTION_TIMEOUT_MS=10000
DB_MAX_POOL_SIZE=10
DB_MIN_IDLE=2
DB_MAX_LIFETIME_MS=1800000
~~~

## Production startup guard

When:

~~~text
APP_ENV=production
~~~

the API refuses to start if it detects:

- local/default JWT secret,
- JWT secret shorter than 64 characters,
- COOKIE_SECURE=false,
- COOKIE_SAME_SITE=None,
- wildcard/local/non-HTTPS CORS origins,
- local database URL,
- local/default database password.

This is intentional fail-closed behavior.

## Vercel + managed API

The frontend can be deployed to Vercel and the API to a managed container host, but the public custom domains should remain same-site.

Example:

~~~text
Vercel
app.example.com

Railway / Render / another container host
api.example.com
~~~

Set the web build variable:

~~~text
NEXT_PUBLIC_API_BASE_URL=https://api.example.com/api
~~~

Set the API environment:

~~~text
APP_ENV=production
CORS_ALLOWED_ORIGINS=https://app.example.com
COOKIE_SECURE=true
COOKIE_SAME_SITE=Lax
~~~

Using the platform default domains on unrelated sites may prevent the Lax authentication cookies from behaving as intended. Do not work around this by setting SameSite=None without adding CSRF protection.

## Database migrations

Flyway runs at API startup.

Production policy:

~~~text
clean disabled
validate on migrate
Hibernate DDL = validate
~~~

Back up the managed PostgreSQL database before applying a release that introduces new migrations.

Never edit an already-applied migration in place.

## Health checks

Container/orchestrator readiness:

~~~text
GET /actuator/health/readiness
~~~

Liveness:

~~~text
GET /actuator/health/liveness
~~~

General API status:

~~~text
GET /api/status
~~~

Actuator health details are not publicly expanded.

## Staff bootstrap

Public registration always produces USER.

For a controlled local/demo environment, follow ADMIN_OPERATIONS.md to promote a registered account to:

~~~text
NUTRITIONIST
ADMIN
~~~

Do not add self-service elevation endpoints.

## Backups and restore

Database backups are infrastructure responsibilities.

For a managed PostgreSQL provider:

- enable scheduled backups,
- set a retention window appropriate to the research/demo environment,
- test restoring into a separate database before relying on the policy,
- never test destructive restore procedures against the only production copy.

The application repository does not pretend to implement provider-level snapshots.

## Release workflow

~~~text
feature commit
     ↓
GitHub Actions
     ├── web build
     ├── API + real PostgreSQL
     └── container builds
     ↓
all green
     ↓
deploy API / migrate
     ↓
readiness healthy
     ↓
deploy web
     ↓
smoke test auth + core user flow
~~~

## Post-deploy smoke flow

Verify:

1. public landing page,
2. account registration,
3. onboarding and personalization consent,
4. meal logging,
5. guidance,
6. plan generation,
7. health-record manual entry,
8. alert evaluation,
9. progress and analytics,
10. research consent,
11. personal-data export,
12. logout/login,
13. staff operations with a provisioned staff account,
14. research aggregate workspace with an admin account.

Do not treat synthetic ABDM mock records as evidence of live interoperability.

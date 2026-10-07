# Authentication & Security

## Release model

Aarogya v1.0.0 uses stateless Spring Security authentication with two signed JWT types:

- access token — short-lived, default 15 minutes,
- refresh token — longer-lived, default 30 days.

Both are delivered to the browser using HttpOnly cookies. The UI does not persist tokens in localStorage.

The API also accepts an access JWT through the standard Authorization Bearer header for non-browser clients.

## JWT minimization

Access tokens contain only:

- account UUID as subject,
- token type,
- issued-at timestamp,
- expiry timestamp.

Email, display name and role are not embedded in the JWT.

The authenticated account is loaded from PostgreSQL on request processing, so the server evaluates current role and enabled state instead of trusting stale profile claims in the token.

## Refresh rotation

For each issued refresh JWT:

1. the raw refresh token is delivered to the browser,
2. only its SHA-256 hash is persisted in refresh_sessions,
3. refresh verifies signature and token type,
4. the persisted session must still be active,
5. the old session is revoked,
6. a new access + refresh pair is issued,
7. the new refresh-token hash is persisted.

Raw refresh tokens are never stored in PostgreSQL.

## Passwords

Passwords use BCrypt with work factor 12.

Registration requires 10–72 characters.

Passwords are never logged or included in audit metadata.

## Roles

Defined roles:

- USER
- NUTRITIONIST
- ADMIN

Public registration always creates USER.

Nutrition content operations are available to NUTRITIONIST and ADMIN accounts, with method-level checks reserving publication/source/audit actions for ADMIN.

Research evaluation endpoints are ADMIN-only.

There is no self-service role elevation endpoint.

## Browser cookies

Local defaults:

~~~text
aarogya_access   HttpOnly; Path=/; SameSite=Lax
aarogya_refresh  HttpOnly; Path=/api/auth; SameSite=Lax
~~~

Production requires:

~~~text
COOKIE_SECURE=true
COOKIE_SAME_SITE=Lax or Strict
~~~

The current release does not implement a cross-site CSRF-token protocol.

Because authentication uses cookies, production deployment must keep browser and API same-site. The startup guard rejects SameSite=None in production.

A future true cross-site deployment must implement explicit CSRF protection before changing that rule.

## CORS

Configured production origins must be explicit HTTPS origins.

The production guard rejects:

- wildcard origins,
- localhost,
- 127.0.0.1,
- non-HTTPS production origins.

Credentialed CORS allows only the headers needed by the current API:

- Content-Type,
- Authorization,
- Accept.

## Production startup guard

When APP_ENV=production the API refuses to start if:

- JWT secret is the local default,
- JWT secret is shorter than 64 characters,
- secure cookies are disabled,
- SameSite=None is configured,
- CORS origins are unsafe,
- database URL points to localhost,
- database password uses the local default.

This prevents a production service from silently booting with development security settings.

## Authentication rate limiting

The following POST endpoints are rate-limited:

- /api/auth/register
- /api/auth/login
- /api/auth/refresh

Default limit:

~~~text
20 requests per 300 seconds
~~~

The limiter is per application instance and remote address.

A multi-instance/high-traffic deployment should move rate limiting to a shared gateway or shared-state limiter.

## Response hardening

API responses include:

- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- Referrer-Policy: no-referrer
- restrictive Permissions-Policy
- restrictive API Content-Security-Policy
- Cache-Control no-store for /api responses

The Next.js web layer also emits baseline frame, MIME, referrer and browser-permission headers.

## Audit privacy

Security-significant actions are persisted in security_audit_events.

New authentication events do not use raw email addresses as audit subjects:

- authenticated events use account UUID references,
- failed-login correlation uses a SHA-256 email fingerprint.

Passwords and raw tokens are never written to the audit stream.

## Personal-data export

Authenticated users can export their own stored application data through:

~~~text
GET /api/account/export
~~~

The export excludes password hashes and raw refresh tokens.

The export itself is auditable.

## Account deletion

Normal USER accounts can delete their account through:

~~~text
DELETE /api/account
~~~

The request requires the current password.

Before deletion, account-linked audit subjects/metadata are anonymized. User-owned rows then follow database cascade rules and browser session cookies are cleared.

Staff accounts cannot self-delete because nutrition content review history must remain attributable. Staff offboarding is an operator-controlled procedure.

## Public security endpoints

Public:

- GET /api/status
- GET /actuator/health
- GET /actuator/health/liveness
- GET /actuator/health/readiness
- GET /actuator/info
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/refresh
- POST /api/auth/logout

Everything else requires authentication unless explicitly restricted further by role.

## Container/runtime security

Production container images:

- run application processes as non-root users,
- expose health checks,
- keep Flyway clean disabled,
- validate schema on startup,
- use graceful API shutdown,
- use bounded database-pool settings.

## Known boundaries

The v1.0.0 academic prototype deliberately does not implement:

- MFA,
- password-reset email/SMS provider,
- distributed/shared rate-limit storage,
- cross-site cookie + CSRF-token mode,
- production key rotation service,
- live ABDM/ABHA authentication,
- medical-device controls.

Those require separate infrastructure/security/regulatory work rather than simulated functionality.

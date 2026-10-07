# Authentication & Security

## Phase 3 model

Aarogya uses stateless Spring Security authentication with two signed JWT types:

- access token — short-lived, default 15 minutes,
- refresh token — longer-lived, default 30 days.

Both are delivered to the browser using HttpOnly cookies. The UI intentionally does not persist tokens in localStorage.

The API also accepts an access JWT through the standard `Authorization: Bearer ...` header for non-browser clients.

## Refresh rotation

Refresh tokens are not treated as permanently valid bearer strings.

For each issued refresh JWT:

1. the raw refresh token is delivered to the client,
2. only its SHA-256 hash is persisted in `refresh_sessions`,
3. refresh verifies the JWT signature and token type,
4. the persisted session must still be active,
5. the old session is revoked,
6. a new access JWT + refresh JWT are issued,
7. the new refresh-token hash is persisted.

This provides server-side revocation and rotation while avoiding storage of raw refresh credentials.

## Passwords

Passwords use BCrypt with work factor 12.

The registration API currently requires 10–72 characters. Passwords are never logged or included in audit metadata.

## Roles

Phase 3 defines:

- `USER`
- `NUTRITIONIST`
- `ADMIN`

Public self-registration always receives `USER`.

Admin APIs are already reserved behind `ROLE_ADMIN`; account provisioning for elevated roles will be built with the relevant administration phase rather than exposed through public registration.

## Browser cookies

Default local behavior:

```text
aarogya_access   HttpOnly; Path=/; SameSite=Lax
aarogya_refresh  HttpOnly; Path=/api/auth; SameSite=Lax
```

Production should set:

```text
COOKIE_SECURE=true
JWT_SECRET=<strong random secret>
```

Cross-site deployment topology must be reviewed before changing SameSite to `None`. A first-party domain or reverse proxy is preferred; if cross-site credential cookies become necessary, explicit CSRF protection must be added rather than simply weakening SameSite.

## Authorization

Public:
- `GET /api/status`
- `GET /actuator/health`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

Authenticated:
- `GET /api/auth/me`
- future user APIs

Admin:
- `/api/admin/**`

## Audit foundation

The `security_audit_events` table records security-significant outcomes without storing passwords or raw tokens.

Phase 3 writes successful registration/login/refresh/logout events and failed credential logins.

Future phases can extend the same audit stream for:
- role changes,
- consent changes,
- health-record access,
- exports,
- administrative mutations.

## Deliberate limitations

This phase does not implement:
- social login,
- SMS OTP,
- password reset email,
- MFA,
- production key rotation,
- a cross-site cookie deployment model.

Those should only be introduced with a concrete requirement rather than as decorative features.

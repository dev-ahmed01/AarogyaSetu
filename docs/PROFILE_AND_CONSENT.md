# Health Profile & Consent

## Phase 4 objective

Phase 4 introduces the first real personalization context for authenticated users while deliberately minimizing the amount of identifying or sensitive data collected.

The profile is not a medical record and must not be represented as one.

## Data minimization

Aarogya does not request:

- exact GPS location,
- street address,
- full date of birth,
- Aadhaar / ABHA identifiers,
- clinical documents,
- prescriptions,
- free-text medical histories.

Phase 4 stores only the context needed for future nutrition features:

- age in years,
- optional sex used for nutrition calculations,
- optional height and weight,
- activity level,
- dietary pattern,
- broad state / region,
- goals,
- food allergies,
- optional self-reported health context.

## Self-reported health context

Health-context selections are user-provided facts for future safety filtering.

They are not diagnoses produced by Aarogya.

The supported Phase 4 contexts are:

- diabetes,
- hypertension,
- anaemia,
- high cholesterol,
- PCOS,
- thyroid condition,
- kidney condition,
- pregnancy or breastfeeding,
- other.

Later recommendation logic must treat higher-risk contexts conservatively and must not turn them into treatment advice.

## Consent architecture

Consent is not a boolean field on `health_profiles`.

Each change creates a new row in `consent_records` with:

- user,
- consent type,
- granted / revoked status,
- policy version,
- timestamp.

Current policy:

```text
consent type:   HEALTH_PROFILE_PERSONALIZATION
policy version: 2026-10
```

The API rejects profile writes unless the latest personalization consent is granted.

Revoking consent pauses personalization but does not delete existing profile data in Phase 4. Data export/deletion and retention controls belong in production-hardening work and must not be falsely represented as implemented before then.

## Onboarding flow

```text
Account created
    ↓
1. About you
    ↓
2. Daily rhythm
    ↓
3. Goals
    ↓
4. Optional safety context
    ↓
5. Explicit consent + review
    ↓
Profile persisted
    ↓
Onboarding marked complete
```

The user can leave onboarding before consent. In that path, the wizard does not send the in-memory profile to the API.

## API

Authenticated routes:

- `GET /api/profile`
- `PUT /api/profile`
- `PUT /api/profile/consent`
- `POST /api/profile/complete`

## Completion requirements

A personalized onboarding is complete only when:

- age is present,
- activity level is present,
- dietary pattern is present,
- at least one goal is present,
- latest personalization consent is granted.

Height, weight, sex-for-nutrition, region, allergies and health contexts remain optional.

## Audit events

Phase 4 adds these security/audit events through the existing audit stream:

- `PROFILE_UPDATED`
- `CONSENT_CHANGED`
- `ONBOARDING_COMPLETED`

No raw profile body is copied into audit metadata.

## UI integrity

The authenticated Today dashboard no longer displays fabricated calorie, fibre or nutrition-score values.

Until meal tracking and the recommendation engine exist, it shows profile readiness and honest empty/future states instead.

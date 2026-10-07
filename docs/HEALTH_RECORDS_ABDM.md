# Health Records & ABDM Architecture

## Phase 9 objective

Phase 9 adds a longitudinal health-record domain and an explicit ABDM/ABHA integration boundary.

The implementation is intentionally split into:

```text
Aarogya internal health-record model
        +
ABDM adapter interface
        +
deterministic local mock adapter
```

There is **no live ABDM or ABHA connectivity in this repository**.

The mock exists so the architecture and UX can be demonstrated without government credentials, network access or misrepresentation.

## ABDM concepts used as architecture guidance

Official ABDM material describes:

- ABHA as a citizen health-account building block,
- PHR applications as a way to link/store/access records,
- consent-based sharing of records,
- interoperability as a core ecosystem goal,
- FHIR alignment for healthcare-information exchange.

Aarogya uses those concepts to shape module boundaries.

It does not claim that the mock adapter implements the current live ABDM API contract.

Official reference material:

- https://abdm.gov.in/
- https://abdm.gov.in/strapicms/uploads/ABHA_Brochure_Piano_final_b972459053.pdf
- https://abdm.gov.in/static/media/Session%202%20-Promoting%20Interoperability%20in%20Digital%20Health.5740ed1d8ec896443683.pdf
- https://abdm.gov.in/static/media/health_management_policy_bac9429a79.80f74bc3e039c00acd4f.pdf

## Internal record model

The internal model deliberately does not store arbitrary external payloads as the application's primary query model.

```text
health_records
    |
    +-- health_record_observations
```

A record envelope stores:

- record type,
- title,
- summary,
- clinical date,
- provider/facility metadata,
- source type,
- source system,
- external source reference,
- interoperability resource type,
- verification state,
- provenance label,
- source payload hash,
- import timestamp.

Structured observations store:

- local/source code,
- coding system,
- display name,
- numeric or text value,
- unit,
- optional reference-range text,
- observation timestamp,
- source observation reference.

This gives later phases a stable internal contract even if an external adapter changes.

## Record types

Phase 9 supports:

```text
LAB_REPORT
PRESCRIPTION
DISCHARGE_SUMMARY
OP_CONSULT
IMMUNIZATION
MEASUREMENT_SET
OTHER
```

## Provenance states

### MANUAL

```text
source_type = MANUAL
source_system = AAROGYA_MANUAL
verification_status = SELF_REPORTED
```

Manual records are explicitly self-reported.

Aarogya does not convert them into "verified clinical records".

### ABDM_MOCK

```text
source_type = ABDM_MOCK
source_system = ABDM_MOCK
verification_status = MOCK_IMPORTED
```

Mock-imported records are synthetic.

They are labelled as synthetic in:

- adapter descriptor,
- record title/summary,
- provenance label,
- UI status,
- integration disclaimer.

## Adapter boundary

```text
AbdmHealthRecordAdapter
    |
    +-- descriptor()
    +-- createExternalSubjectRef()
    +-- fetchRecords()
```

The current implementation is:

```text
MockAbdmHealthRecordAdapter
```

The adapter reports:

```text
integration_mode = MOCK
live_connectivity = false
```

Its subject identifier begins with:

```text
DEMO-SUBJECT-
```

It is intentionally not formatted as an ABHA number.

## Mock dataset

The deterministic mock adapter returns three synthetic records:

1. preventive lab panel,
2. vital measurements,
3. outpatient consultation summary.

Example observations include synthetic HbA1c, haemoglobin, body-weight and blood-pressure values.

These are not interpreted clinically and are not currently consumed by the recommendation engine.

## Duplicate-safe imports

External records have a stable source record reference.

The database enforces uniqueness across:

```text
user
+
source_system
+
source_record_ref
```

A repeated import skips existing records rather than duplicating them.

## Consent boundary

Health-record analysis has its own append-only consent purpose:

```text
HEALTH_RECORD_ANALYSIS
```

Policy version:

```text
2026-10-health-v1
```

This consent is separate from:

- health-profile personalization consent,
- the user's explicit action to store a manual record,
- the user's explicit action to import mock records.

Therefore:

```text
Import/store record
        !=
Allow future wellness analysis
```

Phase 9 stores the analysis-consent decision, but current Phase 7 recommendations still do not read health records.

Phase 10 may use this consent gate before creating health-aware nudges.

## Audit trail

Security audit events are recorded for:

- manual record creation,
- local record deletion,
- health-record analysis consent changes,
- mock integration connect,
- mock integration import,
- mock integration disconnect.

Deleting an imported record removes only Aarogya's local copy.

Disconnecting the mock integration does not silently delete already imported local copies.

## API

Authenticated record endpoints:

```text
GET    /api/health/summary
GET    /api/health/records
GET    /api/health/records/{recordId}
POST   /api/health/records
DELETE /api/health/records/{recordId}

GET    /api/health/consent
PUT    /api/health/consent
```

Mock-integration endpoints:

```text
GET    /api/health/integrations/abdm/status
POST   /api/health/integrations/abdm/connect
POST   /api/health/integrations/abdm/import
DELETE /api/health/integrations/abdm/connection
```

## UI

The authenticated route is:

```text
/health
```

The page uses:

```text
Summary strip
      ↓
Record timeline  |  detail / add panel
      ↓
Mock ABDM source |  separate analysis consent
```

The interface deliberately keeps provenance visible and avoids presenting the mock connection as a live government service.

## Phase boundary

Phase 9 stores and displays records.

It does not:

- diagnose from observations,
- apply lab reference ranges automatically,
- generate disease-specific diet advice,
- use imported observations inside Phase 7 recommendations,
- connect to live ABDM infrastructure.

Health-aware interpretation belongs to Phase 10 and must be consent-gated.

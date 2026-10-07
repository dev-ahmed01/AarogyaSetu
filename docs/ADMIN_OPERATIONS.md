# Nutritionist & Admin Operations

## Phase 14 objective

Phase 14 adds a staff operations layer for nutrition content without turning the application into unrestricted database CRUD.

The console is available at:

~~~text
/admin
~~~

It is intentionally outside the normal user navigation hierarchy and appears only for staff roles.

## Roles

### USER

No administration access.

The UI does not show an Operations link and the API rejects /api/admin/** requests.

### NUTRITIONIST

May:

- view the content review queue,
- inspect food provenance and curation history,
- view the provenance-source registry,
- attach reviewed food-composition provenance,
- replace curated core nutrient data for non-published foods,
- replace the default portion for non-published foods,
- mark complete content READY_TO_PUBLISH.

May not:

- publish or unpublish food,
- return content on behalf of final administration review,
- create provenance sources,
- inspect the security audit,
- view aggregate user / meal / health-record counts.

### ADMIN

May perform all nutritionist actions plus:

- publish food,
- unpublish food,
- return content for changes,
- register provenance sources,
- inspect recent operational audit events,
- view aggregate platform counts.

## Privacy boundary

The administration console does not provide a user-health-record browser.

Staff do not receive:

- individual meal logs,
- health-record contents,
- user health context,
- recommendation details,
- consent payloads.

The admin overview may show aggregate counts only.

Nutritionists do not receive even those platform-level user / meal / health-record counts.

## Content lifecycle

Food curation status is independent from nutrient status.

~~~text
NEEDS_REVIEW
     ↓
IN_REVIEW
     ↓
READY_TO_PUBLISH
     ↓
PUBLISHED
     ↓
UNPUBLISHED
~~~

An admin can return non-published work to NEEDS_REVIEW.

Every transition creates an append-only food_curation_reviews event.

## Existing catalog migration

Phase 14 migration:

~~~text
V12__admin_content_curation.sql
~~~

Existing source-referenced foods migrate to:

~~~text
PUBLISHED
~~~

Existing regional discovery foods with pending nutrient data migrate to:

~~~text
NEEDS_REVIEW
~~~

Their existing discovery behavior remains unchanged until staff begins curated nutrition replacement.

## Safe editing rule

Published content cannot be edited in place.

To change a published food:

1. ADMIN unpublishes it.
2. NUTRITIONIST or ADMIN saves reviewed nutrition/provenance.
3. The food becomes IN_REVIEW and remains inactive.
4. Staff marks it READY_TO_PUBLISH.
5. ADMIN publishes it.

This prevents half-reviewed values from leaking into meal logging while content is being edited.

## Publish-ready invariant

The server, not the browser, decides whether a food is publish-ready.

Required:

- nutrient_status = SOURCE_REFERENCED,
- source type = FOOD_COMPOSITION,
- source food / recipe reference,
- default portion,
- ENERGY_KCAL in kcal,
- PROTEIN_G in g,
- CARBOHYDRATE_G in g,
- FAT_G in g,
- FIBRE_G in g.

The five core values are stored per 100g.

Extra nutrients are supported by the backend request contract, but the current operations UI focuses on the required core set.

## Historical integrity

Meal entries and diet-plan items already snapshot:

- food name,
- portion label,
- quantity,
- provenance reference,
- nutrient values.

Catalog portion foreign keys use ON DELETE SET NULL.

Therefore replacing or removing a current catalog portion does not rewrite historical meals or plans.

Unpublishing a food prevents future catalog use but does not erase prior user history.

## Provenance registry

ADMIN may register a source with one of these source types:

~~~text
FOOD_COMPOSITION
EDITORIAL
GUIDANCE_REFERENCE
REGULATORY_REFERENCE
~~~

Only FOOD_COMPOSITION sources may be attached to publishable nutrient values.

Creating a source does not itself make food publishable.

## Audit

Sensitive operational changes also create security audit events, including:

- food curation update,
- marked ready,
- publish,
- unpublish,
- return for changes,
- nutrition-source creation.

The audit view shows operational metadata and actor identity, not private health-record contents.

## API

Staff:

~~~text
GET /api/admin/overview
GET /api/admin/foods
GET /api/admin/foods/{foodId}
PUT /api/admin/foods/{foodId}/curation
POST /api/admin/foods/{foodId}/ready
GET /api/admin/sources
~~~

ADMIN only:

~~~text
POST /api/admin/foods/{foodId}/publish
POST /api/admin/foods/{foodId}/unpublish
POST /api/admin/foods/{foodId}/return
POST /api/admin/sources
GET /api/admin/audit
~~~

Both HTTP security and method-level authorization enforce the role boundaries.

## Local staff provisioning

There is deliberately no public or self-service role-elevation endpoint.

For local development / academic demo:

1. Register the account normally through Aarogya.
2. Stop using that browser session.
3. Promote the account directly in the local PostgreSQL database as the trusted operator.
4. Sign in again so a new JWT is issued with the staff role.

Example:

~~~sql
UPDATE user_accounts
SET role = 'ADMIN',
    updated_at = NOW()
WHERE lower(email) = lower('admin@example.com');
~~~

For a nutritionist:

~~~sql
UPDATE user_accounts
SET role = 'NUTRITIONIST',
    updated_at = NOW()
WHERE lower(email) = lower('nutritionist@example.com');
~~~

Production deployment must use an operator-controlled identity / provisioning process rather than exposing this ability to application users.

## UI hierarchy

The operations route has three focused views:

~~~text
Review queue
    ↓
selected food
    ↓
provenance + core nutrition
    ↓
decision
    ↓
append-only review history

Sources
    ↓
provenance registry
    ↓
admin-only source registration

Audit (ADMIN)
    ↓
recent operational events
~~~

This avoids a dense multi-widget enterprise dashboard while keeping all publication evidence visible before a decision.

# UX Architecture

## Current route map

```text
/
└── public product / research landing

/signup
└── account creation
    └── redirects to /onboarding

/login
└── sign in

/onboarding
└── focused authenticated setup
    ├── about you
    ├── daily rhythm
    ├── goals
    ├── optional safety context
    └── consent + review

/profile
└── current profile
    ├── personalization state
    ├── consent state
    ├── goals
    └── self-reported safety context

/dashboard
└── Today
    ├── setup status
    ├── current profile foundation
    └── truthful feature-empty states

/foods
└── nutrition knowledge library
    ├── name / alias search
    ├── category / diet / region filters
    ├── evidence-state filter
    ├── master result list
    └── inline detail + provenance

/guidance
└── explainable personalized guidance
    ├── engine status + data window
    ├── what Aarogya noticed
    ├── why it matters
    ├── what you could consider
    └── rule / version / evidence

/meals
└── daily meal logging
    ├── date navigation
    ├── compact daily totals
    ├── breakfast / lunch / dinner / snacks
    ├── contextual food search
    ├── favourites + recent foods
    ├── portion / gram controls
    └── edit / remove

/plans
└── smart planning workspace
    ├── plan date
    ├── persisted draft meal sketch
    ├── example nutrient totals
    ├── per-item explanation/provenance
    ├── regenerate / clear draft
    └── flexible smart food suggestions

/alerts
└── focused alert inbox
    ├── active / attention / snoozed summary
    ├── explainable alert cards
    ├── one primary destination per alert
    ├── snooze / acknowledge / dismiss
    └── optional history

/regional
└── regional familiarity workspace
    ├── self-reported region basis
    ├── no precise-location statement
    ├── familiar foods / dishes
    ├── localized aliases
    ├── planning-eligible vs discovery-only status
    └── ranking-boundary explanation

/health
└── longitudinal health-record workspace
    ├── compact record / observation summary
    ├── chronological timeline
    ├── one record detail / add panel
    ├── visible provenance
    ├── mock ABDM source controls
    └── separate health-record analysis consent

/progress
└── consistency and milestone workspace
    ├── current / longest logging run
    ├── total logging days
    ├── one optional weekly logging goal
    ├── pause / resume / target adjustment
    ├── finite milestone achievements
    └── link to longitudinal analytics

/analytics
└── longitudinal evidence workspace
    ├── 7 / 30-day window control
    ├── current / previous coverage
    ├── daily nutrition bars
    ├── logged-day nutrient comparisons
    ├── meal-pattern counts
    ├── deterministic descriptive insights
    ├── consent-aware manual health series
    └── data-quality boundary

/admin
└── staff operations workspace
    ├── Review queue
    │   ├── food list by curation state
    │   ├── selected food provenance
    │   ├── five core nutrient fields
    │   ├── default portion
    │   ├── role-appropriate review action
    │   └── append-only review history
    ├── Sources
    │   ├── provenance registry
    │   └── admin-only source creation
    └── Audit (ADMIN)
        └── recent operational events

/research
└── admin-only research evaluation workspace
    ├── bounded date window
    ├── opted-in participant count
    ├── privacy-thresholded metrics
    ├── feature-exposure summaries
    ├── dietary-pattern cohorts
    ├── versioned metric definitions
    ├── interpretation boundary
    └── aggregate CSV export
```

## Primary product flow

```text
Account
  ↓
Focused onboarding
  ↓
Explicit personalization consent
  ↓
Today
  ↓
Browse canonical food knowledge
  ↓
Log / review meals
  ↓
Understand remaining targets
  ↓
See explainable guidance
  ↓
Act on a recommendation
  ↓
Review progress later
```

## Onboarding hierarchy

Onboarding is deliberately outside the full application shell so navigation does not compete with setup.

Five steps are used because they reduce density:

1. About you
2. Daily rhythm
3. Goals
4. Optional safety context
5. Consent and review

The user can leave before finishing.

Health profile data remains in component memory until consent is explicitly granted and the final save begins.

## Food-library hierarchy

The food library uses a master/detail pattern rather than separate pages for every record.

The user can search and filter on the left, then inspect:

- nutrient state,
- nutrients,
- portions,
- aliases,
- allergens,
- ingredient structure,
- provenance

in one secondary panel.

This keeps data inspection available without turning the primary navigation into a long list of nutrition tools.

## Information architecture rule

Health records are not the home screen.

The product starts from what the user can understand and act on today. Health records, analytics and provenance remain accessible but progressively disclosed.

## Product-integrity rule

The dashboard and meal surfaces must not display fabricated personalized metrics.

Regional dishes may be discoverable before their nutrition composition is approved, but they stay visibly unloggable and publish no made-up nutrient numbers.

## Interaction hierarchy

Each page should have:
- one page-level primary action,
- contextual secondary actions,
- no duplicated CTA wording competing for attention.

## Meal-logging hierarchy

Meal logging uses the day as the primary object. Daily totals are visually secondary to the four meal slots, and add/edit controls live in one contextual panel rather than opening a dense modal or separate route.

The Today dashboard can now surface real logged totals, but it still does not interpret them as personalized success/failure before Phase 7.

## Guidance hierarchy

Guidance is deliberately not a primary navigation destination. Today surfaces only the highest-priority item and links to `/guidance` for explanation. This keeps the application from turning recommendations into a persistent alarm surface.

The detailed view exposes reasoning and evidence instead of a score-first interface.

## Plan hierarchy

Plans intentionally separate a structured example day from a flexible candidate list.

The draft meal sketch is a planning artifact, not a meal-log mutation. Example nutrient totals describe the generated portions and are never presented as prescribed daily targets.

Each item retains a "Why this appeared" explanation so Phase 7 reasoning remains visible after food ranking.

## Health-record hierarchy

Health records are intentionally not turned into a diagnostic dashboard.

The primary hierarchy is timeline first, record detail second, provenance always visible. Mock integration controls and analytical consent are separated from record browsing so source-management actions do not compete with the health timeline.

Imported demo records are visually labelled synthetic at the row and detail levels.

## Alert hierarchy

Alerts stay outside primary workspace navigation. A small top-bar entry carries only the active count.

Today surfaces at most one highest-priority active nudge. The full alert inbox owns lifecycle actions. Guidance remains the evidence/explanation destination.

This prevents the application from duplicating warning cards across every screen.

## Progress hierarchy

Progress does not lead with a score.

The screen answers three questions in order:

1. Is the user building a useful logging rhythm?
2. What weekly consistency target did the user choose?
3. Which finite milestones have already been earned?

A missed day never removes an achievement, and the UI avoids "streak broken" or failure language.

Longitudinal nutrient analysis remains separate in `/analytics` so consistency and interpretation do not compete for attention.

## Analytics hierarchy

Analytics belongs to the Progress navigation family but has its own route. It leads with data coverage before any trend, keeps unlogged days visibly missing, and uses descriptive rather than evaluative language. Manual health observations appear as dated values without diagnostic bands or success/failure coloring.

## Research hierarchy

Research participation is controlled from `/profile` and is visually separate from personalization consent.

The admin-only `/research` route leads with the privacy boundary and evaluation window before metrics. Suppressed small cohorts display a suppression state rather than a number.

Metric definitions are visible on the same screen so a researcher can understand exactly what each result means before exporting it.

The route never provides drill-down to an individual participant, meal record or health record.

## Future route ownership

Production hardening is deferred to Phase 16.

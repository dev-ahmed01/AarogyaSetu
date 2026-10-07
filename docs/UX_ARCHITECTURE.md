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

/health
└── longitudinal health-record workspace
    ├── compact record / observation summary
    ├── chronological timeline
    ├── one record detail / add panel
    ├── visible provenance
    ├── mock ABDM source controls
    └── separate health-record analysis consent

/progress
└── longitudinal progress workspace placeholder
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

## Future route ownership

- Phase 13: progress analytics

Admin and research routes are deferred to their corresponding phases.

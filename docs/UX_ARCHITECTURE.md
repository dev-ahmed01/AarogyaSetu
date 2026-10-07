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

/meals
└── meal workspace placeholder

/plans
└── diet-plan workspace placeholder

/health
└── health-record workspace placeholder

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

## Information architecture rule

Health records are not the home screen.

The product starts from what the user can understand and act on today. Health records, analytics and provenance remain accessible but progressively disclosed.

## Product-integrity rule

Once real authentication/profile data exists, the dashboard must not display fabricated personalized metrics.

Therefore Phase 4 removes the Phase 2 preview calorie/fibre/score values from the authenticated dashboard. Real nutrition totals return only when meal logging and nutrition computation exist.

## Interaction hierarchy

Each page should have:
- one page-level primary action,
- contextual secondary actions,
- no duplicated CTA wording competing for attention.

## Future route ownership

- Phase 5: nutrition knowledge/catalog foundations
- Phase 6: meal workflows
- Phase 8: plan workflows
- Phase 9: health-record workflows
- Phase 13: progress analytics

Admin and research routes are deferred to their corresponding phases.

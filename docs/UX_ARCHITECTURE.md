# UX Architecture

## Phase 2 route map

```text
/
└── public product / research landing

/dashboard
└── Today
    ├── priority
    ├── nutrition score preview
    ├── meal status
    ├── daily targets
    ├── health-context preview
    └── explainable recommendation preview

/meals
└── meal workspace placeholder

/plans
└── diet-plan workspace placeholder

/health
└── health-record workspace placeholder

/progress
└── longitudinal progress workspace placeholder
```

The placeholders are intentional. Phase 2 establishes navigation and empty-state behavior without fabricating feature completeness.

## Primary product flow

Future phases should preserve this flow:

```text
Onboarding
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

## Information architecture rule

Health records are not the home screen.

The product starts from what the user can understand and act on today. Health records, analytics and provenance remain accessible but progressively disclosed.

## Interaction hierarchy

Each page should have:

- one page-level primary action,
- contextual secondary actions,
- no duplicated CTA wording competing for attention.

## Phase 2 preview data

Dashboard values are intentionally labeled or presented as preview state. They exist to validate hierarchy and component composition, not to imply a working recommendation engine before the relevant phases are implemented.

## Future route ownership

- Phase 3: authentication routes
- Phase 4: onboarding/profile routes
- Phase 6: meal workflows
- Phase 8: plan workflows
- Phase 9: health-record workflows
- Phase 13: progress analytics

Admin and research routes are deferred to their corresponding phases.

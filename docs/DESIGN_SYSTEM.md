# Aarogya Design System

## Design intent

Aarogya borrows the **visual hierarchy principles** of the supplied Purrfect reference, not its pet-travel branding.

The shared DNA is:

- warm, low-contrast backgrounds rather than sterile white-on-blue software,
- a strong display/type hierarchy for major moments,
- readable sans-serif UI copy,
- generous whitespace,
- restrained card density,
- rounded but disciplined surfaces,
- clear primary actions,
- compact supporting metadata,
- progressive disclosure,
- responsive grids that become simpler on small screens.

Aarogya translates that language into a calmer health-and-nutrition identity.

## Core principle

> The user's next useful action should be easier to see than the system's full capability.

That means the interface should not try to prove sophistication through density.

## Tokens

### Color

```text
Canvas            #FBFAF7
Surface           #FFFFFF
Surface soft      #F3F6EF
Surface warm      #FBF1E9

Primary           #4F7D61
Primary dark      #365C46
Primary soft      #E8EFE4

Warm accent       #D98E5F
Warm accent dark  #B96F44

Text              #253029
Muted             #6B756F
Muted light       #89918D

Border            #E5E9E3
Border strong     #D6DCD3

Warning soft      #FFF4DF
Danger soft       #FCEAE7
```

Red is reserved for genuinely important safety/error states. It is not a decorative accent.

### Typography

Display:
- Georgia / serif fallback in the foundation
- used for hero and major page headings
- never for dense data tables or controls

UI:
- Inter / DM Sans / system sans fallback stack
- minimum comfortable body size: 15px where space allows
- supporting metadata: 11–13px

The serif/sans contrast intentionally mirrors the strong headline/body separation in the Purrfect reference.

### Radius

```text
small             10px
control           12px
card              16px
feature surface   22px
pill              999px
```

### Width

Primary content width:
- marketing: about 1160px
- application workspace: about 1120px

## Page hierarchy

Every application page should follow this order unless the feature has a strong reason not to:

1. page context / eyebrow,
2. page title,
3. one-line explanation,
4. primary action,
5. current priority,
6. current work,
7. history / deeper details.

Historical information must not compete with today's action by default.

## Navigation

### Marketing

Top navigation with:
- Aarogya identity,
- small number of explanatory links,
- one clear product CTA.

### Product workspace

Desktop:
- persistent top bar,
- slim left navigation,
- content area.

Mobile/tablet:
- top bar,
- horizontally scrollable compact route navigation,
- no permanently open side rail.

Primary workspace destinations:

- Today
- Meals
- Plans
- Health
- Progress

Additional settings, privacy, admin and research surfaces are introduced only when their functional phases arrive.

## Core components

Phase 2 establishes:

- Button / LinkButton
- Surface
- StatusChip
- ProgressBar
- FormField
- SelectField
- EmptyState
- Skeleton
- PageHeader
- AppShell

Future features should extend these rather than inventing isolated styling per page.

## Dashboard rule

The dashboard is **not** an analytics dump.

Above the fold should communicate:

- what matters today,
- whether something needs attention,
- the clearest next action.

Supporting cards may show:
- meals,
- current goals,
- limited health context.

Longitudinal graphs belong in Progress.

## Recommendation rule

A recommendation surface must be able to expose:

- recommendation,
- reason,
- user context used,
- source/provenance where relevant,
- wellness/safety classification.

The UI must never imply diagnosis when the system has only nutritional or self-reported context.

## Forms

- Labels stay visible above controls.
- Placeholder text is not a label.
- Hints are secondary and concise.
- Error states must explain how to recover.
- Health information should never be requested merely because the database can store it.
- Longer onboarding is broken into focused steps.

## Empty states

An empty screen must answer:

1. Why is this empty?
2. Is that expected?
3. What can the user do next?

Phase 2 placeholder routes intentionally demonstrate this structure.

## Responsive behavior

At <= 980px:
- side navigation becomes compact mobile navigation,
- two-column app sections become single-column as needed.

At <= 760px:
- full-width primary actions are preferred where helpful,
- marketing preview collapses,
- page actions move below the title,
- health priority cards stack vertically.

## Accessibility foundation

- visible keyboard focus,
- semantic navigation landmarks,
- reduced-motion support,
- text contrast prioritized over decorative subtlety,
- controls target roughly 44px minimum height,
- no information encoded only by color.

## Prohibited patterns

Avoid:

- giant KPI walls,
- five competing accent colors,
- decorative medical red,
- tiny body text,
- icon-only navigation for core tasks,
- unexplained scores,
- unexplained recommendations,
- modal-heavy primary workflows,
- horizontal dashboard carousels hiding important actions.

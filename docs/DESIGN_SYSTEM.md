# Aarogya Design System

## Source direction

The interface carries forward the strongest visual-hierarchy ideas from the supplied Purrfect reference:

- generous whitespace,
- restrained card density,
- clear headline/body contrast,
- warm backgrounds instead of sterile hospital white,
- rounded but disciplined controls,
- one dominant action per context,
- progressive disclosure,
- compact supporting text,
- responsive grids.

It does **not** copy Purrfect's pet-travel identity.

## Aarogya visual language

Aarogya should feel calm, trustworthy, human and contemporary rather than clinical or dashboard-heavy.

### Type

- Display: Georgia fallback in Phase 1; a dedicated serif/webfont can be introduced with the full UI system.
- UI/body: system sans stack for speed and clarity.
- Body copy should rarely drop below 15px.
- Dense data should be secondary to the user's next action.

### Core tokens

```text
Canvas          #FBFAF7
Surface         #FFFFFF
Surface soft    #F3F6EF
Primary         #4F7D61
Primary dark    #365C46
Accent warm     #D98E5F
Text            #253029
Muted text      #6B756F
Border          #E5E9E3
Warning soft    #FFF4DF
Danger soft     #FCEAE7
```

### Radius

- controls: 12px
- normal cards: 16px
- feature cards: 22px
- pills: 999px

### Layout

Desktop content width should generally remain between 1120px and 1240px.

Use a hierarchy of:

1. page intent,
2. current priority,
3. supporting actions,
4. historical/details.

Do not put all available health metrics above the fold.

## Interaction rules

- One primary CTA per panel.
- Avoid icon-only actions unless universally understood.
- Empty states should explain the next action.
- Alerts must explain *why* they exist.
- Recommendation cards must expose source/reason context.
- Medical-looking red should be reserved for genuinely important safety states.

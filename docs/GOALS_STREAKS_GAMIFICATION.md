# Goals, Streaks & Gentle Gamification

## Phase 12 objective

Phase 12 creates a behavioral-feedback layer without turning nutrition into a score game.

The implementation rewards **useful repeated data**, not body weight, calories, fasting, restriction, or a supposedly "perfect" diet.

The core model is:

```text
meal-log dates
     ↓
consistency metrics
     ├── weekly goal progress
     ├── current run
     ├── longest run
     └── total logging days
             ↓
       milestone evaluation
```

## Weekly goal

The first user-configurable goal is:

```text
MEAL_LOGGING_DAYS
```

Metric:

```text
DAYS_WITH_ANY_MEAL_LOG
```

A day counts once if at least one meal entry exists.

The allowed target is:

```text
2–7 days / week
```

Default UI selection:

```text
4 days / week
```

The goal measures whether the user is building a usable record. It does not judge whether the logged food was healthy.

The goal may be paused and resumed.

Pausing does not delete data, streak history, or achievements.

## Forgiving streak rule

A current logging run is based on consecutive dates containing at least one meal entry.

The rule deliberately avoids morning failure states:

```text
if today is logged:
    anchor = today

else if yesterday is logged:
    anchor = yesterday

else:
    current run = 0
```

Therefore opening Aarogya early today does not immediately break yesterday's run.

If a later gap exists, the current run becomes zero, but:

- longest run remains,
- total logging days remain,
- earned milestones remain.

The UI avoids "streak broken", "failure", or punishment language.

## Achievements

Initial milestones are intentionally small and finite:

```text
FIRST_LOG
    1 total logging day

THREE_DAY_RUN
    longest logging run >= 3 days

SEVEN_LOGGING_DAYS
    7 total logging days

FOURTEEN_LOGGING_DAYS
    14 total logging days
```

Achievements are append-only for a user.

Once earned, a later missed day cannot revoke them.

There is no points economy, leaderboard, daily penalty, loot mechanic, or escalating streak reward.

## Why there are no calorie / weight achievements

Phase 12 does not add achievements for:

- eating below a calorie value,
- weight loss,
- weight gain,
- fasting duration,
- skipping meals,
- hitting nutrient values every day,
- "clean eating",
- avoiding specific foods.

Those mechanics could convert a wellness tool into a harmful pressure system and would also overstate the precision of the current data.

## Database

Migration:

```text
V11__goals_streaks_gamification.sql
```

Tables:

```text
wellness_goal_templates
user_wellness_goals
achievement_definitions
user_achievements
```

## API

Authenticated routes:

```text
GET  /api/progress/overview?date=YYYY-MM-DD
POST /api/progress/evaluate?date=YYYY-MM-DD

PUT  /api/progress/goals/meal-logging
POST /api/progress/goals/meal-logging/pause
POST /api/progress/goals/meal-logging/resume
```

Goal update body:

```json
{
  "targetDaysPerWeek": 4
}
```

## Evaluation side effects

`GET /overview` is read-only.

Achievement materialization happens only through:

```text
POST /evaluate
```

The web Progress screen calls evaluation on load and receives the updated overview.

This keeps read semantics separate from milestone writes.

## Audit

The following are auditable:

- goal create/update,
- goal pause,
- goal resume,
- milestone evaluation when a new achievement is earned.

No free-form meal payload is copied into the audit record.

## UI

`/progress` replaces the former placeholder.

The hierarchy is:

```text
Current rhythm
      ↓
One weekly goal
      ↓
Milestones
      ↓
Phase boundary / explanation
```

There is no points total.

The primary message is:

> Build enough consistency to learn from.

The Progress page explicitly says that Phase 12 measures consistency while Phase 13 will own longitudinal nutrition analytics.

## Phase boundary

Phase 12 does not yet calculate:

- nutrient trend charts,
- moving averages,
- goal-vs-nutrient analytics,
- health-record trends,
- correlations,
- weekly/monthly nutrition reports.

Those belong to Phase 13.

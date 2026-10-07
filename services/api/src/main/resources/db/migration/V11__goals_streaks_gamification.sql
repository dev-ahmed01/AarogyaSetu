CREATE TABLE wellness_goal_templates (
    goal_code        VARCHAR(80) PRIMARY KEY,
    title            VARCHAR(180) NOT NULL,
    description      VARCHAR(700) NOT NULL,
    metric_code      VARCHAR(80) NOT NULL,
    default_target   INTEGER NOT NULL,
    min_target       INTEGER NOT NULL,
    max_target       INTEGER NOT NULL,
    period_code      VARCHAR(30) NOT NULL,
    active           BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_goal_template_target CHECK (
        min_target > 0
        AND default_target BETWEEN min_target AND max_target
    ),
    CONSTRAINT chk_goal_template_period CHECK (
        period_code IN ('WEEK')
    )
);

CREATE TABLE user_wellness_goals (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    goal_code       VARCHAR(80) NOT NULL REFERENCES wellness_goal_templates(goal_code),
    target_value    INTEGER NOT NULL,
    status          VARCHAR(30) NOT NULL,
    started_on      DATE NOT NULL,
    ended_on        DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, goal_code),
    CONSTRAINT chk_user_goal_status CHECK (
        status IN ('ACTIVE', 'PAUSED', 'ARCHIVED')
    ),
    CONSTRAINT chk_user_goal_target CHECK (target_value > 0)
);

CREATE INDEX idx_user_wellness_goals_user_status
    ON user_wellness_goals(user_id, status);

CREATE TABLE achievement_definitions (
    achievement_code  VARCHAR(100) PRIMARY KEY,
    title             VARCHAR(180) NOT NULL,
    description       VARCHAR(700) NOT NULL,
    criteria_code     VARCHAR(100) NOT NULL,
    threshold_value   INTEGER NOT NULL,
    display_order     INTEGER NOT NULL,
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_achievement_threshold CHECK (threshold_value > 0)
);

CREATE TABLE user_achievements (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    achievement_code  VARCHAR(100) NOT NULL REFERENCES achievement_definitions(achievement_code),
    earned_at         TIMESTAMPTZ NOT NULL,
    evidence_value    INTEGER NOT NULL,
    UNIQUE (user_id, achievement_code)
);

CREATE INDEX idx_user_achievements_user
    ON user_achievements(user_id, earned_at DESC);

INSERT INTO wellness_goal_templates (
    goal_code, title, description, metric_code,
    default_target, min_target, max_target, period_code
) VALUES
(
    'MEAL_LOGGING_DAYS',
    'Keep a useful meal record',
    'Choose how many days this week you would like to record at least one meal. This measures consistency only, not dietary quality.',
    'DAYS_WITH_ANY_MEAL_LOG',
    4,
    2,
    7,
    'WEEK'
);

INSERT INTO achievement_definitions (
    achievement_code, title, description, criteria_code,
    threshold_value, display_order
) VALUES
(
    'FIRST_LOG',
    'First record',
    'Logged at least one meal. A small start, with no requirement to keep a perfect streak.',
    'TOTAL_LOGGING_DAYS',
    1,
    1
),
(
    'THREE_DAY_RUN',
    'Three-day rhythm',
    'Built a three-day run of meal logging. Missing a later day does not remove this achievement.',
    'LONGEST_LOGGING_RUN',
    3,
    2
),
(
    'SEVEN_LOGGING_DAYS',
    'Seven useful days',
    'Recorded meals on seven different days in total. The days do not need to be consecutive.',
    'TOTAL_LOGGING_DAYS',
    7,
    3
),
(
    'FOURTEEN_LOGGING_DAYS',
    'Two weeks of evidence',
    'Recorded meals on fourteen different days in total, creating a stronger base for later trend analysis.',
    'TOTAL_LOGGING_DAYS',
    14,
    4
);

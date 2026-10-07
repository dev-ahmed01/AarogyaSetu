-- Phase 15: privacy-safe research participation and evaluation.

CREATE TABLE research_feature_events (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    event_code      VARCHAR(80) NOT NULL,
    event_version   INTEGER NOT NULL,
    event_date      DATE NOT NULL,
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, event_code, event_date)
);

CREATE INDEX idx_research_feature_events_user_date
    ON research_feature_events(user_id, event_date DESC);

CREATE INDEX idx_research_feature_events_code_date
    ON research_feature_events(event_code, event_date DESC);

CREATE TABLE research_metric_definitions (
    metric_code         VARCHAR(100) NOT NULL,
    metric_version      INTEGER NOT NULL,
    name                VARCHAR(220) NOT NULL,
    description         VARCHAR(1200) NOT NULL,
    unit                VARCHAR(50) NOT NULL,
    window_days         INTEGER,
    minimum_cohort_size INTEGER NOT NULL,
    PRIMARY KEY (metric_code, metric_version),
    CONSTRAINT chk_research_metric_min_cohort
        CHECK (minimum_cohort_size >= 5),
    CONSTRAINT chk_research_metric_window
        CHECK (window_days IS NULL OR window_days > 0)
);

INSERT INTO research_metric_definitions (
    metric_code,
    metric_version,
    name,
    description,
    unit,
    window_days,
    minimum_cohort_size
) VALUES
(
    'OPTED_IN_PARTICIPANTS',
    1,
    'Opted-in research participants',
    'Accounts whose latest RESEARCH_PARTICIPATION consent record is granted. This is an operational participation count, not a health outcome.',
    'participants',
    NULL,
    5
),
(
    'ACTIVE_LOGGERS',
    1,
    'Active meal loggers',
    'Opted-in participants with at least one eligible meal-log day in the selected evaluation window after their latest research consent grant.',
    'participants',
    NULL,
    5
),
(
    'LOGGING_DAYS_PER_ACTIVE_PARTICIPANT',
    1,
    'Meal logging days per active participant',
    'Mean distinct meal-log days among opted-in participants who recorded at least one eligible meal in the selected evaluation window.',
    'days',
    NULL,
    5
),
(
    'PRE_POST_LOGGING_DAY_CHANGE_7D',
    1,
    'Associated change after first feature exposure',
    'Mean difference in distinct meal-log days between the seven days before and seven days after a participant first recorded an eligible feature exposure. Exposure day is excluded. This is observational association, not causal effect.',
    'days',
    7,
    5
);

CREATE TABLE nudge_rules (
    id              UUID PRIMARY KEY,
    rule_code       VARCHAR(100) NOT NULL,
    rule_version    INTEGER NOT NULL,
    category        VARCHAR(40) NOT NULL,
    severity        VARCHAR(30) NOT NULL,
    cooldown_hours  INTEGER NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (rule_code, rule_version),
    CONSTRAINT chk_nudge_rule_category CHECK (
        category IN ('SAFETY', 'NUTRITION', 'PROFILE')
    ),
    CONSTRAINT chk_nudge_rule_severity CHECK (
        severity IN ('ATTENTION', 'STANDARD', 'INFO')
    ),
    CONSTRAINT chk_nudge_rule_cooldown CHECK (cooldown_hours >= 0)
);

CREATE TABLE nudge_instances (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    nudge_key           VARCHAR(220) NOT NULL,
    rule_code           VARCHAR(100) NOT NULL,
    rule_version        INTEGER NOT NULL,
    category            VARCHAR(40) NOT NULL,
    severity            VARCHAR(30) NOT NULL,
    status              VARCHAR(30) NOT NULL,
    title               VARCHAR(220) NOT NULL,
    message             VARCHAR(1800) NOT NULL,
    action_label        VARCHAR(120),
    action_href         VARCHAR(240),
    reason_code         VARCHAR(120) NOT NULL,
    source_type         VARCHAR(40) NOT NULL,
    source_ref          VARCHAR(220),
    evidence_label      VARCHAR(260),
    evidence_url        VARCHAR(600),
    first_generated_at  TIMESTAMPTZ NOT NULL,
    last_evaluated_at   TIMESTAMPTZ NOT NULL,
    snoozed_until       TIMESTAMPTZ,
    acknowledged_at     TIMESTAMPTZ,
    dismissed_at        TIMESTAMPTZ,
    resolved_at         TIMESTAMPTZ,
    UNIQUE (user_id, nudge_key),
    CONSTRAINT chk_nudge_status CHECK (
        status IN ('ACTIVE', 'SNOOZED', 'ACKNOWLEDGED', 'DISMISSED', 'RESOLVED')
    ),
    CONSTRAINT chk_nudge_category CHECK (
        category IN ('SAFETY', 'NUTRITION', 'PROFILE')
    ),
    CONSTRAINT chk_nudge_severity CHECK (
        severity IN ('ATTENTION', 'STANDARD', 'INFO')
    ),
    CONSTRAINT chk_nudge_source CHECK (
        source_type IN ('RECOMMENDATION', 'HEALTH_RECORD')
    )
);

CREATE INDEX idx_nudge_instances_user_status
    ON nudge_instances(user_id, status, last_evaluated_at DESC);

INSERT INTO nudge_rules (
    id, rule_code, rule_version, category, severity, cooldown_hours
) VALUES
(
    '60000000-0000-0000-0000-000000000001',
    'ALLERGEN_CONFLICT_NUDGE',
    1,
    'SAFETY',
    'ATTENTION',
    24
),
(
    '60000000-0000-0000-0000-000000000002',
    'DIETARY_PATTERN_REVIEW',
    1,
    'PROFILE',
    'STANDARD',
    168
),
(
    '60000000-0000-0000-0000-000000000003',
    'FIBRE_TREND_NUDGE',
    1,
    'NUTRITION',
    'STANDARD',
    168
),
(
    '60000000-0000-0000-0000-000000000004',
    'PROTEIN_TREND_NUDGE',
    1,
    'NUTRITION',
    'STANDARD',
    168
),
(
    '60000000-0000-0000-0000-000000000005',
    'PROFILE_WEIGHT_REVIEW',
    1,
    'PROFILE',
    'INFO',
    336
);

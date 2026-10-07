CREATE TABLE health_profiles (
    user_id                  UUID PRIMARY KEY REFERENCES user_accounts(id) ON DELETE CASCADE,
    age_years                SMALLINT,
    sex_for_nutrition        VARCHAR(30),
    height_cm                NUMERIC(5,2),
    weight_kg                NUMERIC(5,2),
    activity_level           VARCHAR(30),
    dietary_pattern          VARCHAR(40),
    state_or_region          VARCHAR(80),
    onboarding_completed_at  TIMESTAMPTZ,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_age_years CHECK (age_years IS NULL OR age_years BETWEEN 13 AND 120),
    CONSTRAINT chk_height_cm CHECK (height_cm IS NULL OR height_cm BETWEEN 80 AND 250),
    CONSTRAINT chk_weight_kg CHECK (weight_kg IS NULL OR weight_kg BETWEEN 20 AND 350)
);

CREATE TABLE profile_goals (
    user_id      UUID NOT NULL REFERENCES health_profiles(user_id) ON DELETE CASCADE,
    goal_code    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, goal_code)
);

CREATE TABLE profile_allergies (
    user_id       UUID NOT NULL REFERENCES health_profiles(user_id) ON DELETE CASCADE,
    allergy_code  VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, allergy_code)
);

CREATE TABLE profile_health_contexts (
    user_id       UUID NOT NULL REFERENCES health_profiles(user_id) ON DELETE CASCADE,
    context_code  VARCHAR(60) NOT NULL,
    PRIMARY KEY (user_id, context_code)
);

CREATE TABLE consent_records (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    consent_type     VARCHAR(60) NOT NULL,
    granted          BOOLEAN NOT NULL,
    policy_version   VARCHAR(30) NOT NULL,
    recorded_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_consent_records_user_type_time
    ON consent_records(user_id, consent_type, recorded_at DESC);

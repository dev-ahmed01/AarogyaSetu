CREATE TABLE health_records (
    id                              UUID PRIMARY KEY,
    user_id                         UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    record_type                     VARCHAR(50) NOT NULL,
    title                           VARCHAR(220) NOT NULL,
    summary_text                    VARCHAR(1600),
    clinical_date                   DATE NOT NULL,
    provider_name                   VARCHAR(220),
    facility_name                   VARCHAR(220),
    source_type                     VARCHAR(30) NOT NULL,
    source_system                   VARCHAR(80) NOT NULL,
    source_record_ref               VARCHAR(160),
    interoperability_resource_type  VARCHAR(80),
    verification_status             VARCHAR(40) NOT NULL,
    provenance_label                VARCHAR(300) NOT NULL,
    source_payload_hash             VARCHAR(64),
    imported_at                     TIMESTAMPTZ,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_health_record_type CHECK (
        record_type IN (
            'LAB_REPORT',
            'PRESCRIPTION',
            'DISCHARGE_SUMMARY',
            'OP_CONSULT',
            'IMMUNIZATION',
            'MEASUREMENT_SET',
            'OTHER'
        )
    ),
    CONSTRAINT chk_health_record_source_type CHECK (
        source_type IN ('MANUAL', 'ABDM_MOCK')
    ),
    CONSTRAINT chk_health_record_verification CHECK (
        verification_status IN ('SELF_REPORTED', 'MOCK_IMPORTED')
    )
);

CREATE INDEX idx_health_records_user_date
    ON health_records(user_id, clinical_date DESC, created_at DESC);

CREATE UNIQUE INDEX uk_health_record_external_ref
    ON health_records(user_id, source_system, source_record_ref)
    WHERE source_record_ref IS NOT NULL;

CREATE TABLE health_record_observations (
    id                      UUID PRIMARY KEY,
    health_record_id        UUID NOT NULL REFERENCES health_records(id) ON DELETE CASCADE,
    observation_code        VARCHAR(100) NOT NULL,
    coding_system           VARCHAR(120) NOT NULL,
    display_name            VARCHAR(180) NOT NULL,
    value_numeric           NUMERIC(16,4),
    value_text              VARCHAR(500),
    unit                    VARCHAR(40),
    reference_range_text    VARCHAR(160),
    observed_at             TIMESTAMPTZ,
    source_observation_ref  VARCHAR(160),
    CONSTRAINT chk_health_observation_value CHECK (
        value_numeric IS NOT NULL OR value_text IS NOT NULL
    )
);

CREATE INDEX idx_health_observations_record
    ON health_record_observations(health_record_id);

CREATE INDEX idx_health_observations_code
    ON health_record_observations(observation_code);

CREATE TABLE health_integrations (
    id                         UUID PRIMARY KEY,
    user_id                    UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    provider_code              VARCHAR(80) NOT NULL,
    display_name               VARCHAR(180) NOT NULL,
    integration_mode           VARCHAR(30) NOT NULL,
    status                     VARCHAR(30) NOT NULL,
    live_connectivity          BOOLEAN NOT NULL DEFAULT FALSE,
    interoperability_standard  VARCHAR(120),
    external_subject_ref       VARCHAR(180),
    connected_at               TIMESTAMPTZ,
    disconnected_at            TIMESTAMPTZ,
    last_imported_at           TIMESTAMPTZ,
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, provider_code),
    CONSTRAINT chk_health_integration_mode CHECK (
        integration_mode IN ('MOCK', 'LIVE')
    ),
    CONSTRAINT chk_health_integration_status CHECK (
        status IN ('CONNECTED', 'DISCONNECTED')
    )
);

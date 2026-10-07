CREATE TABLE app_metadata (
    id              BIGSERIAL PRIMARY KEY,
    metadata_key    VARCHAR(100) NOT NULL UNIQUE,
    metadata_value  VARCHAR(500) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO app_metadata (metadata_key, metadata_value)
VALUES
    ('schema_version', 'phase-1'),
    ('product_mode', 'academic-research-prototype');

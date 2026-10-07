CREATE TABLE user_accounts (
    id              UUID PRIMARY KEY,
    email           VARCHAR(320) NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    display_name    VARCHAR(120) NOT NULL,
    role            VARCHAR(30) NOT NULL,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'NUTRITIONIST', 'ADMIN'))
);

CREATE INDEX idx_user_accounts_role ON user_accounts(role);

CREATE TABLE refresh_sessions (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    token_hash      CHAR(64) NOT NULL UNIQUE,
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    last_used_at    TIMESTAMPTZ,
    revoked_at      TIMESTAMPTZ
);

CREATE INDEX idx_refresh_sessions_user_id ON refresh_sessions(user_id);
CREATE INDEX idx_refresh_sessions_expires_at ON refresh_sessions(expires_at);

CREATE TABLE security_audit_events (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID REFERENCES user_accounts(id) ON DELETE SET NULL,
    event_type      VARCHAR(80) NOT NULL,
    event_outcome   VARCHAR(30) NOT NULL,
    subject         VARCHAR(320),
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    metadata_json   TEXT
);

CREATE INDEX idx_security_audit_user_id ON security_audit_events(user_id);
CREATE INDEX idx_security_audit_occurred_at ON security_audit_events(occurred_at DESC);

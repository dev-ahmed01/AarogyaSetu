-- Phase 16 release hardening: align refresh-token hash storage with JPA mapping.
-- Existing SHA-256 hashes are 64 characters, so this changes representation only.

ALTER TABLE refresh_sessions
    ALTER COLUMN token_hash TYPE VARCHAR(64);

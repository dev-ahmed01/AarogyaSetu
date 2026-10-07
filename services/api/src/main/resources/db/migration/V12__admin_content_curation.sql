-- Phase 14: nutritionist/admin operations and controlled food publication.

ALTER TABLE foods
    ADD COLUMN curation_status VARCHAR(40) NOT NULL DEFAULT 'NEEDS_REVIEW';

UPDATE foods
SET curation_status = CASE
    WHEN nutrient_status = 'SOURCE_REFERENCED' THEN 'PUBLISHED'
    ELSE 'NEEDS_REVIEW'
END;

ALTER TABLE foods
    ADD CONSTRAINT chk_food_curation_status
    CHECK (
        curation_status IN (
            'NEEDS_REVIEW',
            'IN_REVIEW',
            'READY_TO_PUBLISH',
            'PUBLISHED',
            'UNPUBLISHED'
        )
    );

CREATE INDEX idx_foods_curation_status
    ON foods(curation_status);

CREATE TABLE food_curation_reviews (
    id                  UUID PRIMARY KEY,
    food_id             UUID NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    reviewer_user_id    UUID NOT NULL REFERENCES user_accounts(id) ON DELETE RESTRICT,
    reviewer_role       VARCHAR(30) NOT NULL,
    review_action       VARCHAR(50) NOT NULL,
    from_status         VARCHAR(40) NOT NULL,
    to_status           VARCHAR(40) NOT NULL,
    review_note         VARCHAR(1000),
    occurred_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_food_curation_reviews_food
    ON food_curation_reviews(food_id, occurred_at DESC);

CREATE INDEX idx_food_curation_reviews_reviewer
    ON food_curation_reviews(reviewer_user_id, occurred_at DESC);

CREATE INDEX idx_security_audit_events_occurred_at
    ON security_audit_events(occurred_at DESC);

CREATE INDEX idx_security_audit_events_type_time
    ON security_audit_events(event_type, occurred_at DESC);

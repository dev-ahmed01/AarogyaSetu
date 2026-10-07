-- Phase 7: explainable recommendation engine foundations.

ALTER TABLE meal_entries
    ADD COLUMN dietary_classification_snapshot VARCHAR(50);

UPDATE meal_entries entry
SET dietary_classification_snapshot = food.dietary_classification
FROM foods food
WHERE entry.food_id = food.id;

ALTER TABLE meal_entries
    ALTER COLUMN dietary_classification_snapshot SET NOT NULL;

CREATE TABLE meal_entry_allergens (
    meal_entry_id  UUID NOT NULL REFERENCES meal_entries(id) ON DELETE CASCADE,
    allergen_code  VARCHAR(50) NOT NULL,
    PRIMARY KEY (meal_entry_id, allergen_code)
);

INSERT INTO meal_entry_allergens (meal_entry_id, allergen_code)
SELECT entry.id, allergen.allergen_code
FROM meal_entries entry
JOIN food_allergens allergen ON allergen.food_id = entry.food_id
ON CONFLICT DO NOTHING;

-- Explicit zero-fibre records make coverage complete for the current
-- nutrient-capable dairy/egg seed foods rather than treating "missing" as zero.
INSERT INTO food_nutrients (
    id, food_id, nutrient_code, amount_per_100g, unit, source_id, source_food_ref
) VALUES
(
    '40000000-0000-0000-0000-000000000066',
    '20000000-0000-0000-0000-000000000007',
    'FIBRE_G',
    0,
    'g',
    '10000000-0000-0000-0000-000000000001',
    'NDB:01077'
),
(
    '40000000-0000-0000-0000-000000000076',
    '20000000-0000-0000-0000-000000000008',
    'FIBRE_G',
    0,
    'g',
    '10000000-0000-0000-0000-000000000001',
    'NDB:01116'
),
(
    '40000000-0000-0000-0000-000000000086',
    '20000000-0000-0000-0000-000000000009',
    'FIBRE_G',
    0,
    'g',
    '10000000-0000-0000-0000-000000000001',
    'NDB:01129'
)
ON CONFLICT (food_id, nutrient_code) DO NOTHING;

CREATE TABLE recommendation_evidence_sources (
    id            UUID PRIMARY KEY,
    source_code   VARCHAR(100) NOT NULL UNIQUE,
    name          VARCHAR(220) NOT NULL,
    version_label VARCHAR(100),
    source_url    VARCHAR(600) NOT NULL,
    license_note  VARCHAR(300),
    retrieved_on  DATE NOT NULL
);

CREATE TABLE recommendation_rules (
    id                    UUID PRIMARY KEY,
    rule_code             VARCHAR(100) NOT NULL,
    rule_version          INTEGER NOT NULL,
    title                 VARCHAR(180) NOT NULL,
    rule_type             VARCHAR(50) NOT NULL,
    nutrient_code         VARCHAR(60),
    reference_value       NUMERIC(12,4),
    reference_unit        VARCHAR(40),
    trigger_ratio         NUMERIC(6,4),
    minimum_observed_days SMALLINT NOT NULL DEFAULT 0,
    minimum_age_years     SMALLINT,
    maximum_age_years     SMALLINT,
    reason_code           VARCHAR(100) NOT NULL,
    safety_class          VARCHAR(50) NOT NULL,
    evidence_source_id    UUID REFERENCES recommendation_evidence_sources(id),
    active                BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (rule_code, rule_version)
);

CREATE INDEX idx_recommendation_rules_active
    ON recommendation_rules(active, rule_type);

INSERT INTO recommendation_evidence_sources (
    id, source_code, name, version_label, source_url, license_note, retrieved_on
) VALUES
(
    '50000000-0000-0000-0000-000000000001',
    'WHO_HEALTHY_DIET_2026',
    'World Health Organization — Healthy diet',
    '2026 current fact sheet',
    'https://www.who.int/en/news-room/fact-sheets/detail/healthy-diet',
    'WHO source; retain attribution. Prototype stores only rule metadata and concise reference values.',
    '2026-10-07'
),
(
    '50000000-0000-0000-0000-000000000002',
    'WHO_FAO_UNU_PROTEIN_2007',
    'WHO/FAO/UNU — Protein and amino acid requirements in human nutrition',
    'WHO Technical Report Series 935',
    'https://iris.who.int/handle/10665/43411',
    'WHO source; retain attribution. Adult safe-level reference only.',
    '2026-10-07'
),
(
    '50000000-0000-0000-0000-000000000003',
    'FSSAI_ALLERGEN_REFERENCE',
    'FSSAI — Labelling and Display Regulations allergen reference',
    'regulatory reference',
    'https://www.fssai.gov.in/',
    'Reference metadata only; Aarogya does not diagnose allergy or reaction risk.',
    '2026-10-07'
),
(
    '50000000-0000-0000-0000-000000000004',
    'AAROGYA_PRODUCT_HEURISTIC',
    'Aarogya research-prototype evaluation heuristic',
    'phase-7-v1',
    'https://github.com/dev-ahmed01/AarogyaSetu',
    'Project-authored non-clinical heuristic. Not a dietary guideline.',
    '2026-10-07'
);

INSERT INTO recommendation_rules (
    id, rule_code, rule_version, title, rule_type,
    nutrient_code, reference_value, reference_unit, trigger_ratio,
    minimum_observed_days, minimum_age_years, maximum_age_years,
    reason_code, safety_class, evidence_source_id
) VALUES
(
    '51000000-0000-0000-0000-000000000001',
    'FIBRE_TREND_LOW',
    1,
    'Fibre has been below the reference level',
    'NUTRIENT_TREND',
    'FIBRE_G',
    25,
    'g/day',
    0.80,
    2,
    13,
    NULL,
    'FIBRE_BELOW_REFERENCE_TREND',
    'WELLNESS',
    '50000000-0000-0000-0000-000000000001'
),
(
    '51000000-0000-0000-0000-000000000002',
    'PROTEIN_TREND_LOW',
    1,
    'Protein has been below the adult reference level',
    'WEIGHT_SCALED_NUTRIENT_TREND',
    'PROTEIN_G',
    0.83,
    'g/kg/day',
    0.80,
    2,
    18,
    NULL,
    'PROTEIN_BELOW_ADULT_REFERENCE_TREND',
    'WELLNESS',
    '50000000-0000-0000-0000-000000000002'
),
(
    '51000000-0000-0000-0000-000000000003',
    'ALLERGEN_CONFLICT',
    1,
    'A logged food conflicts with a recorded allergy',
    'SAFETY_CONFLICT',
    NULL,
    NULL,
    NULL,
    NULL,
    0,
    NULL,
    NULL,
    'LOGGED_ALLERGEN_CONFLICT',
    'SAFETY_ATTENTION',
    '50000000-0000-0000-0000-000000000003'
),
(
    '51000000-0000-0000-0000-000000000004',
    'DIETARY_PATTERN_CONFLICT',
    1,
    'A logged food does not match the selected dietary pattern',
    'PROFILE_CONSISTENCY',
    NULL,
    NULL,
    NULL,
    NULL,
    0,
    NULL,
    NULL,
    'LOGGED_DIET_PATTERN_CONFLICT',
    'INFORMATIONAL',
    '50000000-0000-0000-0000-000000000004'
);

CREATE TABLE regional_evidence_sources (
    source_code   VARCHAR(100) PRIMARY KEY,
    name          VARCHAR(220) NOT NULL,
    source_url    VARCHAR(600),
    usage_note    VARCHAR(1000) NOT NULL
);

CREATE TABLE food_localized_aliases (
    id           UUID PRIMARY KEY,
    food_id      UUID NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    locale_code  VARCHAR(20) NOT NULL,
    alias        VARCHAR(180) NOT NULL,
    UNIQUE (food_id, locale_code, alias)
);

CREATE INDEX idx_food_localized_aliases_alias
    ON food_localized_aliases (lower(alias));

CREATE TABLE food_region_affinities (
    id              UUID PRIMARY KEY,
    food_id         UUID NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    region_code     VARCHAR(80) NOT NULL,
    affinity_score  SMALLINT NOT NULL,
    relationship    VARCHAR(40) NOT NULL,
    rationale       VARCHAR(700) NOT NULL,
    source_code     VARCHAR(100) NOT NULL REFERENCES regional_evidence_sources(source_code),
    UNIQUE (food_id, region_code),
    CONSTRAINT chk_region_affinity_score CHECK (
        affinity_score BETWEEN 0 AND 100
    ),
    CONSTRAINT chk_region_relationship CHECK (
        relationship IN ('STATE_FAMILIAR', 'REGIONAL_FAMILIAR', 'ALL_INDIA')
    )
);

CREATE INDEX idx_food_region_affinity_region
    ON food_region_affinities(region_code, affinity_score DESC);

CREATE TABLE regional_food_alternatives (
    id                   UUID PRIMARY KEY,
    source_food_id       UUID NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    alternative_food_id  UUID NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    region_code          VARCHAR(80) NOT NULL,
    priority             SMALLINT NOT NULL DEFAULT 0,
    rationale            VARCHAR(700) NOT NULL,
    active               BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (source_food_id, alternative_food_id, region_code),
    CONSTRAINT chk_regional_alternative_not_self CHECK (
        source_food_id <> alternative_food_id
    )
);

ALTER TABLE diet_plans
    ADD COLUMN regional_context_code VARCHAR(80),
    ADD COLUMN regional_context_label VARCHAR(120);

ALTER TABLE diet_plan_items
    ADD COLUMN regional_fit_score SMALLINT,
    ADD COLUMN regional_fit_label VARCHAR(60),
    ADD COLUMN regional_reason_snapshot VARCHAR(700);

INSERT INTO regional_evidence_sources (
    source_code, name, source_url, usage_note
) VALUES
(
    'AAROGYA_REGIONAL_HEURISTIC',
    'Aarogya regional familiarity heuristic',
    'https://github.com/dev-ahmed01/AarogyaSetu',
    'Project-authored cultural familiarity metadata used only to rank already-safe catalog choices. It is not a dietary guideline or nutrition source.'
),
(
    'FSSAI_MILLET_REGIONAL_REFERENCE',
    'FSSAI — Millets and regional recipes',
    'https://fssai.gov.in/upload/EATRIGHTINDIA/English.pdf',
    'Reference demonstrating regional diversity and millet culinary traditions. Nutrient values are not copied from this publication into Aarogya.'
),
(
    'ICMR_NIN_DGI_2024',
    'ICMR-NIN — Dietary Guidelines for Indians 2024',
    'https://www.nin.res.in/dietaryguidelines/',
    'Guidance reference only. Regional affinity scores are not represented as ICMR-NIN recommendations.'
);

INSERT INTO food_region_affinities (
    id, food_id, region_code, affinity_score, relationship, rationale, source_code
) VALUES
('71000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','ALL_INDIA',70,'ALL_INDIA','Common catalog fruit retained as a broadly familiar fallback across regions.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','SOUTH_INDIA',76,'REGIONAL_FAMILIAR','Broad South India familiarity metadata; used only as a small ranking bonus.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000002','ALL_INDIA',72,'ALL_INDIA','Rice is retained as a broadly familiar staple option.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000002','SOUTH_INDIA',86,'REGIONAL_FAMILIAR','Broad South India familiarity metadata; not a claim about preferred intake.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000002','EAST_INDIA',84,'REGIONAL_FAMILIAR','Broad East India familiarity metadata; not a claim about preferred intake.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000003','ALL_INDIA',70,'ALL_INDIA','Chickpeas remain available as a broad pulse option.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000003','NORTH_INDIA',84,'REGIONAL_FAMILIAR','Broad North India familiarity metadata for chickpea-based food contexts.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000004','ALL_INDIA',78,'ALL_INDIA','Lentils remain a broad pulse option across regional contexts.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000004','SOUTH_INDIA',80,'REGIONAL_FAMILIAR','Broad South India familiarity metadata for lentil-based meal contexts.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000004','NORTH_INDIA',79,'REGIONAL_FAMILIAR','Broad North India familiarity metadata for lentil-based meal contexts.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000005','ALL_INDIA',70,'ALL_INDIA','Leafy-green catalog option retained as a broad fallback.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000006','ALL_INDIA',66,'ALL_INDIA','Groundnut remains available where allergy filters permit it.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000006','WEST_INDIA',82,'REGIONAL_FAMILIAR','Broad West India familiarity metadata for groundnut contexts.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000006','SOUTH_INDIA',78,'REGIONAL_FAMILIAR','Broad South India familiarity metadata for groundnut contexts.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000015','20000000-0000-0000-0000-000000000007','ALL_INDIA',75,'ALL_INDIA','Milk remains a broad catalog option where diet/allergy filters permit it.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000016','20000000-0000-0000-0000-000000000008','ALL_INDIA',74,'ALL_INDIA','Curd/yogurt remains a broad catalog option where diet/allergy filters permit it.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000017','20000000-0000-0000-0000-000000000008','SOUTH_INDIA',82,'REGIONAL_FAMILIAR','Broad South India familiarity metadata for curd/yogurt contexts.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000018','20000000-0000-0000-0000-000000000009','ALL_INDIA',68,'ALL_INDIA','Egg remains a broad option only for compatible dietary patterns.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000019','20000000-0000-0000-0000-000000000101','STATE_KARNATAKA',100,'STATE_FAMILIAR','Karnataka discovery dish. Nutrition remains pending curation and this affinity does not make it loggable.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000020','20000000-0000-0000-0000-000000000102','SOUTH_INDIA',94,'REGIONAL_FAMILIAR','South India discovery dish. Nutrition remains pending curation.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000021','20000000-0000-0000-0000-000000000103','SOUTH_INDIA',94,'REGIONAL_FAMILIAR','South India discovery dish. Nutrition remains pending curation.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000022','20000000-0000-0000-0000-000000000104','NORTH_INDIA',94,'REGIONAL_FAMILIAR','North India discovery dish. Nutrition remains pending curation.','AAROGYA_REGIONAL_HEURISTIC'),
('71000000-0000-0000-0000-000000000023','20000000-0000-0000-0000-000000000105','ALL_INDIA',86,'ALL_INDIA','Whole-wheat roti is retained as a broad discovery dish while recipe nutrient curation remains pending.','AAROGYA_REGIONAL_HEURISTIC');

INSERT INTO food_localized_aliases (id, food_id, locale_code, alias) VALUES
('72000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','kn-IN','ಬಾಳೆಹಣ್ಣು'),
('72000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000001','hi-IN','केला'),
('72000000-0000-0000-0000-000000000003','20000000-0000-0000-0000-000000000002','kn-IN','ಅನ್ನ'),
('72000000-0000-0000-0000-000000000004','20000000-0000-0000-0000-000000000002','hi-IN','चावल'),
('72000000-0000-0000-0000-000000000005','20000000-0000-0000-0000-000000000003','hi-IN','चना'),
('72000000-0000-0000-0000-000000000006','20000000-0000-0000-0000-000000000004','kn-IN','ಬೇಳೆ'),
('72000000-0000-0000-0000-000000000007','20000000-0000-0000-0000-000000000004','hi-IN','दाल'),
('72000000-0000-0000-0000-000000000008','20000000-0000-0000-0000-000000000005','hi-IN','पालक'),
('72000000-0000-0000-0000-000000000009','20000000-0000-0000-0000-000000000006','kn-IN','ಕಡಲೆಕಾಯಿ'),
('72000000-0000-0000-0000-000000000010','20000000-0000-0000-0000-000000000006','hi-IN','मूंगफली'),
('72000000-0000-0000-0000-000000000011','20000000-0000-0000-0000-000000000007','kn-IN','ಹಾಲು'),
('72000000-0000-0000-0000-000000000012','20000000-0000-0000-0000-000000000007','hi-IN','दूध'),
('72000000-0000-0000-0000-000000000013','20000000-0000-0000-0000-000000000008','kn-IN','ಮೊಸರು'),
('72000000-0000-0000-0000-000000000014','20000000-0000-0000-0000-000000000008','hi-IN','दही'),
('72000000-0000-0000-0000-000000000015','20000000-0000-0000-0000-000000000009','kn-IN','ಮೊಟ್ಟೆ'),
('72000000-0000-0000-0000-000000000016','20000000-0000-0000-0000-000000000009','hi-IN','अंडा'),
('72000000-0000-0000-0000-000000000017','20000000-0000-0000-0000-000000000101','kn-IN','ರಾಗಿ ಮುದ್ದೆ'),
('72000000-0000-0000-0000-000000000018','20000000-0000-0000-0000-000000000102','kn-IN','ಇಡ್ಲಿ'),
('72000000-0000-0000-0000-000000000019','20000000-0000-0000-0000-000000000103','kn-IN','ಸಾಂಬಾರ್'),
('72000000-0000-0000-0000-000000000020','20000000-0000-0000-0000-000000000104','hi-IN','राजमा चावल'),
('72000000-0000-0000-0000-000000000021','20000000-0000-0000-0000-000000000105','hi-IN','रोटी');

INSERT INTO regional_food_alternatives (
    id, source_food_id, alternative_food_id, region_code, priority, rationale
) VALUES
(
    '73000000-0000-0000-0000-000000000001',
    '20000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000101',
    'STATE_KARNATAKA',
    1,
    'A Karnataka-familiar staple for discovery. This is not a nutrient-equivalent substitution and ragi mudde remains non-loggable until its recipe data is curated.'
),
(
    '73000000-0000-0000-0000-000000000002',
    '20000000-0000-0000-0000-000000000004',
    '20000000-0000-0000-0000-000000000103',
    'SOUTH_INDIA',
    1,
    'A South India-familiar lentil-based dish for discovery. Recipe variation means nutrition equivalence is not implied.'
),
(
    '73000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000003',
    '20000000-0000-0000-0000-000000000104',
    'NORTH_INDIA',
    1,
    'A North India-familiar pulse-and-rice meal for discovery. It is not treated as nutritionally interchangeable with chickpeas.'
);

CREATE TABLE diet_plans (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    plan_date         DATE NOT NULL,
    status            VARCHAR(30) NOT NULL,
    generation_mode   VARCHAR(50) NOT NULL,
    engine_status     VARCHAR(60) NOT NULL,
    source_rule_code  VARCHAR(100),
    source_rule_version INTEGER,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_diet_plan_status CHECK (status IN ('DRAFT','ARCHIVED'))
);

CREATE INDEX idx_diet_plans_user_date
    ON diet_plans(user_id, plan_date, created_at DESC);

CREATE TABLE diet_plan_items (
    id                              UUID PRIMARY KEY,
    plan_id                         UUID NOT NULL REFERENCES diet_plans(id) ON DELETE CASCADE,
    meal_type                       VARCHAR(20) NOT NULL,
    display_order                   SMALLINT NOT NULL,
    food_id                         UUID NOT NULL REFERENCES foods(id) ON DELETE RESTRICT,
    food_name_snapshot              VARCHAR(180) NOT NULL,
    dietary_classification_snapshot VARCHAR(50) NOT NULL,
    portion_id                      UUID REFERENCES food_portions(id) ON DELETE SET NULL,
    portion_label_snapshot          VARCHAR(100),
    quantity_grams                  NUMERIC(9,2) NOT NULL,
    nutrient_focus_code             VARCHAR(60),
    reason_code                     VARCHAR(100) NOT NULL,
    explanation                     VARCHAR(1000) NOT NULL,
    source_code_snapshot            VARCHAR(80),
    source_food_ref_snapshot        VARCHAR(120),
    CONSTRAINT chk_diet_plan_meal_type CHECK (
        meal_type IN ('BREAKFAST','LUNCH','DINNER','SNACK')
    ),
    CONSTRAINT chk_diet_plan_quantity CHECK (quantity_grams > 0)
);

CREATE INDEX idx_diet_plan_items_plan
    ON diet_plan_items(plan_id, display_order);

CREATE TABLE diet_plan_item_nutrients (
    id            UUID PRIMARY KEY,
    plan_item_id  UUID NOT NULL REFERENCES diet_plan_items(id) ON DELETE CASCADE,
    nutrient_code VARCHAR(60) NOT NULL,
    amount        NUMERIC(14,4) NOT NULL,
    unit          VARCHAR(20) NOT NULL,
    UNIQUE (plan_item_id, nutrient_code),
    CONSTRAINT chk_plan_item_nutrient_amount CHECK (amount >= 0)
);

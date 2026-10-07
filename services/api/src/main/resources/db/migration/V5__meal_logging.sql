CREATE TABLE meal_entries (
    id                         UUID PRIMARY KEY,
    user_id                    UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    food_id                    UUID NOT NULL REFERENCES foods(id) ON DELETE RESTRICT,
    portion_id                 UUID REFERENCES food_portions(id) ON DELETE SET NULL,
    meal_date                  DATE NOT NULL,
    meal_type                  VARCHAR(20) NOT NULL,
    quantity_grams             NUMERIC(9,2) NOT NULL,
    portion_count              NUMERIC(8,3),
    food_name_snapshot         VARCHAR(180) NOT NULL,
    portion_label_snapshot     VARCHAR(100),
    source_code_snapshot       VARCHAR(80),
    source_food_ref_snapshot   VARCHAR(120),
    nutrient_status_snapshot   VARCHAR(50) NOT NULL,
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_meal_type CHECK (meal_type IN ('BREAKFAST','LUNCH','DINNER','SNACK')),
    CONSTRAINT chk_meal_quantity CHECK (quantity_grams > 0),
    CONSTRAINT chk_meal_portion_count CHECK (portion_count IS NULL OR portion_count > 0)
);

CREATE INDEX idx_meal_entries_user_date
    ON meal_entries(user_id, meal_date, created_at);

CREATE INDEX idx_meal_entries_user_recent
    ON meal_entries(user_id, created_at DESC);

CREATE TABLE meal_entry_nutrients (
    id             UUID PRIMARY KEY,
    meal_entry_id  UUID NOT NULL REFERENCES meal_entries(id) ON DELETE CASCADE,
    nutrient_code  VARCHAR(60) NOT NULL,
    amount         NUMERIC(14,4) NOT NULL,
    unit           VARCHAR(20) NOT NULL,
    UNIQUE (meal_entry_id, nutrient_code),
    CONSTRAINT chk_meal_nutrient_amount CHECK (amount >= 0)
);

CREATE INDEX idx_meal_entry_nutrients_entry
    ON meal_entry_nutrients(meal_entry_id);

CREATE TABLE user_food_favorites (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    food_id     UUID NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, food_id)
);

CREATE INDEX idx_user_food_favorites_user
    ON user_food_favorites(user_id, created_at DESC);

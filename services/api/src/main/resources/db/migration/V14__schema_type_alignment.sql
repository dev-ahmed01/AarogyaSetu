-- Phase 16 release hardening: align legacy SMALLINT columns with Java int/Integer mappings.
-- This is intentionally forward-only; previously applied migrations remain immutable.

ALTER TABLE health_profiles
    ALTER COLUMN age_years TYPE INTEGER;

ALTER TABLE food_portions
    ALTER COLUMN display_order TYPE INTEGER;

ALTER TABLE food_ingredients
    ALTER COLUMN display_order TYPE INTEGER;

ALTER TABLE recommendation_rules
    ALTER COLUMN minimum_observed_days TYPE INTEGER,
    ALTER COLUMN minimum_age_years TYPE INTEGER,
    ALTER COLUMN maximum_age_years TYPE INTEGER;

ALTER TABLE diet_plan_items
    ALTER COLUMN display_order TYPE INTEGER,
    ALTER COLUMN regional_fit_score TYPE INTEGER;

ALTER TABLE food_region_affinities
    ALTER COLUMN affinity_score TYPE INTEGER;

ALTER TABLE regional_food_alternatives
    ALTER COLUMN priority TYPE INTEGER;

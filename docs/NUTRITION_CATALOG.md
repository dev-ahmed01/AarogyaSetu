# Nutrition Knowledge Platform

## Phase 5 objective

Phase 5 creates a source-aware food knowledge layer before meal logging.

The goal is not to maximize the number of foods in the database. The goal is to make every food record explicit about:

- what the food is,
- how users may search for it,
- whether it has usable nutrient data,
- how portions map to grams,
- which allergen flags apply,
- where the record came from,
- whether a regional dish is still awaiting curation.

## Data model

```text
nutrition_sources
foods
food_aliases
food_tags
food_allergens
food_portions
food_nutrients
food_ingredients
```

### Food identity

A food has:

- stable UUID,
- URL/search slug,
- canonical name,
- optional aliases,
- food type,
- category,
- dietary classification,
- broad region,
- nutrient data status,
- provenance source,
- source food reference.

### Nutrient status

Phase 5 uses two honest states:

```text
SOURCE_REFERENCED
PENDING_CURATED_DATA
```

`SOURCE_REFERENCED` means the prototype seed nutrient values are tied to a named public-domain source record and can be used by later prototype calculations.

It does **not** mean production clinical validation has occurred.

`PENDING_CURATED_DATA` means the food can be discovered, tagged and related to ingredients, but Aarogya deliberately publishes no numeric nutrition values for it yet.

That prevents invented calorie/macronutrient numbers for recipe-variable dishes.

## Provenance policy

### USDA FoodData Central

USDA FoodData Central states that its data are public domain and published under CC0 1.0.

Phase 5 uses USDA FoodData Central / SR Legacy references for a small set of nutrient-complete seed foods.

Each nutrient-capable seed keeps a source reference such as:

```text
NDB:09040
NDB:16057
```

The prototype should migrate toward an automated import/reconciliation process before production use.

### ICMR-NIN

ICMR-NIN material is strategically important to the research direction, but the 2024 Dietary Guidelines publication includes restrictions on storing/reproducing its data in an electronic product without prior permission.

Therefore Phase 5 stores ICMR-NIN as **guidance-reference metadata only**.

No NIN numeric nutrition tables are copied into this repository.

Future work may use licensed/permissioned Indian composition data, or an approved data integration, while keeping the same source model.

### FSSAI

FSSAI regulations are retained as a regulatory-reference source for allergen and food-safety semantics.

The Phase 5 allergen taxonomy includes:

- wheat,
- crustacean,
- milk,
- egg,
- fish,
- peanut,
- tree nut,
- soy,
- sulphite.

The product should continue to distinguish regulatory labelling concepts from medical allergy diagnosis.

## Seed catalog

Phase 5 includes nutrient-capable generic foods such as:

- banana,
- cooked white rice,
- cooked chickpeas,
- cooked lentils,
- cooked spinach,
- raw peanuts,
- whole milk,
- plain whole-milk yogurt,
- cooked whole egg.

It also includes regional discovery records such as:

- ragi mudde,
- idli,
- sambar,
- rajma chawal,
- whole-wheat roti.

Regional discovery foods remain `PENDING_CURATED_DATA` until trustworthy nutrient composition is attached.

## Search

Authenticated search supports:

- canonical name,
- aliases,
- category,
- dietary classification,
- broad region,
- nutrient status,
- pagination.

Aliases allow queries such as:

```text
chana  -> Chickpeas
palak  -> Spinach
dahi   -> Plain whole-milk yogurt
chapati -> Whole-wheat roti
```

## API

Authenticated endpoints:

```text
GET /api/nutrition/foods
GET /api/nutrition/foods/{slug}
GET /api/nutrition/metadata
```

Example filters:

```text
/api/nutrition/foods?q=chana
/api/nutrition/foods?category=PULSE
/api/nutrition/foods?dietary=VEGAN
/api/nutrition/foods?region=Karnataka
/api/nutrition/foods?nutrientStatus=SOURCE_REFERENCED
```

## Meal-logging boundary

Phase 6 may only use foods whose catalog response reports:

```text
loggable = true
```

Regional discovery records with pending nutrient data must not silently become meal-calculation inputs.

## UI

The food library lives at:

```text
/foods
```

It uses a master/detail layout so users can inspect nutrient values and provenance without opening multiple dense screens.

The `/meals` page links to the food library now and remains explicit that actual meal logging belongs to Phase 6.

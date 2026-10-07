package in.aarogya.plans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;

import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodNutrient;
import in.aarogya.nutrition.domain.FoodPortion;
import in.aarogya.nutrition.domain.NutritionSource;
import in.aarogya.plans.domain.DietPlan;
import in.aarogya.plans.domain.DietPlanItem;

class DietPlanItemSnapshotTests {

    @Test
    void planItemFreezesFoodPortionSourceAndNutrients() {
        var plan = mock(DietPlan.class);
        var food = mock(Food.class);
        var portion = mock(FoodPortion.class);
        var source = mock(NutritionSource.class);
        var fibre = mock(FoodNutrient.class);

        when(food.getCanonicalName()).thenReturn("Lentils, cooked");
        when(food.getDietaryClassification()).thenReturn("VEGAN");
        when(food.getSource()).thenReturn(source);
        when(food.getSourceFoodRef()).thenReturn("NDB:16070");
        when(food.getNutrients()).thenReturn(Set.of(fibre));
        when(source.getSourceCode()).thenReturn("USDA_FDC");
        when(portion.getLabel()).thenReturn("1 cup cooked");
        when(fibre.getNutrientCode()).thenReturn("FIBRE_G");
        when(fibre.getUnit()).thenReturn("g");
        when(fibre.amountForGrams(new BigDecimal("198.00")))
            .thenReturn(new BigDecimal("15.6420"));

        var item = new DietPlanItem(
            plan,
            "LUNCH",
            1,
            food,
            portion,
            new BigDecimal("198.00"),
            "FIBRE_G",
            "FIBRE_BELOW_REFERENCE_TREND",
            "Source-referenced fibre option."
        );

        assertEquals("Lentils, cooked", item.getFoodNameSnapshot());
        assertEquals("VEGAN", item.getDietaryClassificationSnapshot());
        assertEquals("USDA_FDC", item.getSourceCodeSnapshot());
        assertEquals("NDB:16070", item.getSourceFoodRefSnapshot());
        assertEquals(1, item.getNutrients().size());
        assertNotNull(item.getId());
    }
}

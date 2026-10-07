package in.aarogya.meals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.meals.domain.MealEntry;
import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodNutrient;
import in.aarogya.nutrition.domain.FoodPortion;

class MealEntrySnapshotTests {

    @Test
    void replacingEntryFreezesScaledNutrients() {
        var user = mock(UserAccount.class);
        var food = mock(Food.class);
        var portion = mock(FoodPortion.class);
        var energy = mock(FoodNutrient.class);
        var protein = mock(FoodNutrient.class);

        when(food.getCanonicalName()).thenReturn("Chickpeas, cooked");
        when(food.getNutrientStatus()).thenReturn("SOURCE_REFERENCED");
        when(food.getDietaryClassification()).thenReturn("VEGAN");
        when(food.getAllergens()).thenReturn(Set.of("PEANUT"));
        when(food.getNutrients()).thenReturn(Set.of(energy, protein));

        when(energy.getNutrientCode()).thenReturn("ENERGY_KCAL");
        when(energy.getUnit()).thenReturn("kcal");
        when(energy.amountForGrams(new BigDecimal("164.00")))
            .thenReturn(new BigDecimal("268.9600"));

        when(protein.getNutrientCode()).thenReturn("PROTEIN_G");
        when(protein.getUnit()).thenReturn("g");
        when(protein.amountForGrams(new BigDecimal("164.00")))
            .thenReturn(new BigDecimal("14.5304"));

        var entry = new MealEntry(user);
        entry.replaceSnapshot(
            food,
            portion,
            LocalDate.of(2026, 10, 7),
            "LUNCH",
            new BigDecimal("164.00"),
            BigDecimal.ONE,
            "1 cup cooked",
            "USDA_FDC",
            "NDB:16057"
        );

        assertEquals("Chickpeas, cooked", entry.getFoodNameSnapshot());
        assertEquals(new BigDecimal("164.00"), entry.getQuantityGrams());
        assertEquals("USDA_FDC", entry.getSourceCodeSnapshot());
        assertEquals("VEGAN", entry.getDietaryClassificationSnapshot());
        assertEquals(Set.of("PEANUT"), entry.getAllergenSnapshots());
        assertEquals(2, entry.getNutrientSnapshots().size());
        assertNotNull(entry.getUpdatedAt());
    }
}

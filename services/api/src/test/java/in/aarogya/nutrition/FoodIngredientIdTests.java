package in.aarogya.nutrition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.nutrition.domain.FoodIngredientId;

class FoodIngredientIdTests {

    @Test
    void compositeIngredientIdentityUsesBothFoods() {
        var parent = UUID.randomUUID();
        var ingredient = UUID.randomUUID();

        var first = new FoodIngredientId(parent, ingredient);
        var same = new FoodIngredientId(parent, ingredient);
        var different = new FoodIngredientId(parent, UUID.randomUUID());

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
    }
}

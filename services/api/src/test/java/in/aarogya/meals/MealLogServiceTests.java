package in.aarogya.meals;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.meals.repository.MealEntryRepository;
import in.aarogya.meals.repository.UserFoodFavoriteRepository;
import in.aarogya.meals.service.MealLogService;
import in.aarogya.nutrition.repository.FoodRepository;

class MealLogServiceTests {

    @Test
    void historyRangeIsLimitedToThirtyOneDays() {
        var service = new MealLogService(
            mock(MealEntryRepository.class),
            mock(UserFoodFavoriteRepository.class),
            mock(UserAccountRepository.class),
            mock(FoodRepository.class)
        );

        var userId = java.util.UUID.randomUUID();
        var from = LocalDate.of(2026, 8, 1);
        var to = LocalDate.of(2026, 10, 7);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.history(userId, from, to)
        );
    }
}

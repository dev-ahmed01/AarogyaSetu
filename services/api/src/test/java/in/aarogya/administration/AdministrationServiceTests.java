package in.aarogya.administration;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.administration.api.FoodCurationRequest;
import in.aarogya.administration.repository.FoodCurationReviewRepository;
import in.aarogya.administration.service.AdministrationService;
import in.aarogya.health.repository.HealthRecordRepository;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.meals.repository.MealEntryRepository;
import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.NutritionSource;
import in.aarogya.nutrition.repository.FoodNutrientRepository;
import in.aarogya.nutrition.repository.FoodPortionRepository;
import in.aarogya.nutrition.repository.FoodRepository;
import in.aarogya.nutrition.repository.NutritionSourceRepository;
import in.aarogya.security.AarogyaPrincipal;
import in.aarogya.security.SecurityAuditService;
import in.aarogya.security.repository.SecurityAuditEventRepository;

class AdministrationServiceTests {

    @Test
    void nutritionistOverviewDoesNotExposePlatformUserMetrics() {
        var fixture = fixture();
        var principal = new AarogyaPrincipal(
            UUID.randomUUID(),
            "nutritionist@example.test",
            "Nutritionist",
            "hash",
            "NUTRITIONIST",
            true
        );

        when(fixture.foods.count()).thenReturn(14L);
        when(fixture.foods.countByCurationStatus("NEEDS_REVIEW"))
            .thenReturn(5L);
        when(fixture.foods.countByCurationStatus("IN_REVIEW"))
            .thenReturn(2L);
        when(fixture.foods.countByCurationStatus("READY_TO_PUBLISH"))
            .thenReturn(1L);
        when(fixture.foods.countByCurationStatus("PUBLISHED"))
            .thenReturn(6L);
        when(fixture.foods.countByCurationStatus("UNPUBLISHED"))
            .thenReturn(0L);
        when(fixture.sources.count()).thenReturn(4L);

        var result = fixture.service.overview(principal);

        assertNull(result.userCount());
        assertNull(result.mealEntryCount());
        assertNull(result.healthRecordCount());
        assertNull(result.auditEventCount());

        verify(fixture.users, never()).count();
        verify(fixture.meals, never()).count();
        verify(fixture.health, never()).count();
        verify(fixture.audit, never()).count();
    }

    @Test
    void publishedFoodCannotBeCuratedInPlace() {
        var fixture = fixture();
        var food = mock(Food.class);
        var foodId = UUID.randomUUID();

        when(fixture.foods.findById(foodId))
            .thenReturn(Optional.of(food));
        when(food.getCurationStatus()).thenReturn("PUBLISHED");

        var request = mock(FoodCurationRequest.class);

        assertThrows(
            IllegalStateException.class,
            () -> fixture.service.saveCuration(
                adminPrincipal(),
                foodId,
                request
            )
        );

        verify(fixture.nutrients, never())
            .deleteByFood_Id(foodId);
        verify(fixture.portions, never())
            .deleteByFood_Id(foodId);
    }

    @Test
    void curationRejectsEditorialSourceForPublishableNutrition() {
        var fixture = fixture();
        var food = mock(Food.class);
        var source = mock(NutritionSource.class);
        var foodId = UUID.randomUUID();
        var sourceId = UUID.randomUUID();
        var request = mock(FoodCurationRequest.class);

        when(fixture.foods.findById(foodId))
            .thenReturn(Optional.of(food));
        when(food.getCurationStatus())
            .thenReturn("NEEDS_REVIEW");
        when(request.sourceId()).thenReturn(sourceId);
        when(fixture.sources.findById(sourceId))
            .thenReturn(Optional.of(source));
        when(source.getSourceType()).thenReturn("EDITORIAL");

        assertThrows(
            IllegalArgumentException.class,
            () -> fixture.service.saveCuration(
                adminPrincipal(),
                foodId,
                request
            )
        );

        verify(fixture.nutrients, never())
            .deleteByFood_Id(foodId);
    }

    @Test
    void foodWithoutCuratedDataCannotBeMarkedReady() {
        var fixture = fixture();
        var food = mock(Food.class);
        var foodId = UUID.randomUUID();

        when(fixture.foods.findById(foodId))
            .thenReturn(Optional.of(food));
        when(food.getCurationStatus()).thenReturn("IN_REVIEW");
        when(food.getNutrientStatus())
            .thenReturn("SOURCE_REFERENCED");
        when(food.getSource()).thenReturn(null);
        when(fixture.nutrients
            .findByFood_IdOrderByNutrientCodeAsc(foodId))
            .thenReturn(java.util.List.of());
        when(fixture.portions
            .findByFood_IdOrderByDisplayOrderAsc(foodId))
            .thenReturn(java.util.List.of());

        assertThrows(
            IllegalStateException.class,
            () -> fixture.service.markReady(
                adminPrincipal(),
                foodId,
                "reviewed"
            )
        );
    }

    private AarogyaPrincipal adminPrincipal() {
        return new AarogyaPrincipal(
            UUID.randomUUID(),
            "admin@example.test",
            "Admin",
            "hash",
            "ADMIN",
            true
        );
    }

    private Fixture fixture() {
        var foods = mock(FoodRepository.class);
        var nutrients = mock(FoodNutrientRepository.class);
        var portions = mock(FoodPortionRepository.class);
        var sources = mock(NutritionSourceRepository.class);
        var reviews = mock(FoodCurationReviewRepository.class);
        var users = mock(UserAccountRepository.class);
        var meals = mock(MealEntryRepository.class);
        var health = mock(HealthRecordRepository.class);
        var audit = mock(SecurityAuditEventRepository.class);
        var auditService = mock(SecurityAuditService.class);

        return new Fixture(
            new AdministrationService(
                foods,
                nutrients,
                portions,
                sources,
                reviews,
                users,
                meals,
                health,
                audit,
                auditService
            ),
            foods,
            nutrients,
            portions,
            sources,
            users,
            meals,
            health,
            audit
        );
    }

    private record Fixture(
        AdministrationService service,
        FoodRepository foods,
        FoodNutrientRepository nutrients,
        FoodPortionRepository portions,
        NutritionSourceRepository sources,
        UserAccountRepository users,
        MealEntryRepository meals,
        HealthRecordRepository health,
        SecurityAuditEventRepository audit
    ) {
    }
}

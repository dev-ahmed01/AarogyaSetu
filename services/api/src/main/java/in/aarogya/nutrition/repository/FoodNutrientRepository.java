package in.aarogya.nutrition.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.nutrition.domain.FoodNutrient;

public interface FoodNutrientRepository
    extends JpaRepository<FoodNutrient, UUID> {

    List<FoodNutrient> findByFood_IdOrderByNutrientCodeAsc(UUID foodId);

    long deleteByFood_Id(UUID foodId);
}

package in.aarogya.nutrition.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.nutrition.domain.FoodIngredient;
import in.aarogya.nutrition.domain.FoodIngredientId;

public interface FoodIngredientRepository
    extends JpaRepository<FoodIngredient, FoodIngredientId> {

    List<FoodIngredient> findByParentFood_SlugOrderByDisplayOrderAsc(String slug);
}

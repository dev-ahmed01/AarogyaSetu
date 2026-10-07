package in.aarogya.nutrition.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.nutrition.domain.NutritionSource;

public interface NutritionSourceRepository extends JpaRepository<NutritionSource, UUID> {

    List<NutritionSource> findAllByOrderByNameAsc();
}

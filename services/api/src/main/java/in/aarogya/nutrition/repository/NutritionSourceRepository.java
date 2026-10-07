package in.aarogya.nutrition.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.nutrition.domain.NutritionSource;

public interface NutritionSourceRepository extends JpaRepository<NutritionSource, UUID> {

    List<NutritionSource> findAllByOrderByNameAsc();

    Optional<NutritionSource> findBySourceCodeIgnoreCase(String sourceCode);

    boolean existsBySourceCodeIgnoreCase(String sourceCode);
}

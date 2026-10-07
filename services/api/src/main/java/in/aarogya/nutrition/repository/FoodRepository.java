package in.aarogya.nutrition.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.aarogya.nutrition.domain.Food;

public interface FoodRepository extends JpaRepository<Food, UUID> {

    Optional<Food> findBySlugAndActiveTrue(String slug);

    @Query("""
        select distinct f
        from Food f
        left join f.aliases foodAlias
        where f.active = true
          and (
            :query is null
            or lower(f.canonicalName) like lower(concat('%', :query, '%'))
            or lower(foodAlias) like lower(concat('%', :query, '%'))
          )
          and (:category is null or f.categoryCode = :category)
          and (:dietary is null or f.dietaryClassification = :dietary)
          and (:region is null or lower(f.primaryRegion) = lower(:region))
          and (:nutrientStatus is null or f.nutrientStatus = :nutrientStatus)
        """)
    Page<Food> search(
        @Param("query") String query,
        @Param("category") String category,
        @Param("dietary") String dietary,
        @Param("region") String region,
        @Param("nutrientStatus") String nutrientStatus,
        Pageable pageable
    );

    @Query("select distinct f.categoryCode from Food f where f.active = true order by f.categoryCode")
    List<String> findDistinctCategories();

    @Query("select distinct f.dietaryClassification from Food f where f.active = true order by f.dietaryClassification")
    List<String> findDistinctDietaryClassifications();

    @Query("select distinct f.primaryRegion from Food f where f.active = true and f.primaryRegion is not null order by f.primaryRegion")
    List<String> findDistinctRegions();

    @Query("select distinct f.nutrientStatus from Food f where f.active = true order by f.nutrientStatus")
    List<String> findDistinctNutrientStatuses();
}

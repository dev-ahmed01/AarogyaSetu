package in.aarogya.nutrition.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.nutrition.domain.FoodPortion;

public interface FoodPortionRepository
    extends JpaRepository<FoodPortion, UUID> {

    List<FoodPortion> findByFood_IdOrderByDisplayOrderAsc(UUID foodId);

    long deleteByFood_Id(UUID foodId);
}

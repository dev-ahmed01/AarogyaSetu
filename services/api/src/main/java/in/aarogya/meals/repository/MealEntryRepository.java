package in.aarogya.meals.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.meals.domain.MealEntry;

public interface MealEntryRepository extends JpaRepository<MealEntry, UUID> {

    List<MealEntry> findByUser_IdAndMealDateOrderByCreatedAtAsc(
        UUID userId,
        LocalDate mealDate
    );

    List<MealEntry> findByUser_IdAndMealDateBetweenOrderByMealDateDescCreatedAtAsc(
        UUID userId,
        LocalDate from,
        LocalDate to
    );

    Optional<MealEntry> findByIdAndUser_Id(UUID id, UUID userId);

    List<MealEntry> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}

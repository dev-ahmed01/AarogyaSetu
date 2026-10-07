package in.aarogya.plans.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.plans.domain.DietPlan;

public interface DietPlanRepository extends JpaRepository<DietPlan, UUID> {

    Optional<DietPlan> findTopByUser_IdAndPlanDateAndStatusOrderByCreatedAtDesc(
        UUID userId,
        LocalDate planDate,
        String status
    );

    List<DietPlan> findByUser_IdAndPlanDateAndStatus(
        UUID userId,
        LocalDate planDate,
        String status
    );

    Optional<DietPlan> findByIdAndUser_Id(UUID id, UUID userId);
}

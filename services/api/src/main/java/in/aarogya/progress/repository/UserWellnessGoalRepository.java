package in.aarogya.progress.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.progress.domain.UserWellnessGoal;

public interface UserWellnessGoalRepository
    extends JpaRepository<UserWellnessGoal, UUID> {

    Optional<UserWellnessGoal> findByUser_IdAndTemplate_GoalCode(
        UUID userId,
        String goalCode
    );

    List<UserWellnessGoal> findByUser_IdOrderByCreatedAtAsc(UUID userId);
}

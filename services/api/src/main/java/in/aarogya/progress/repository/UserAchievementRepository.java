package in.aarogya.progress.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.progress.domain.UserAchievement;

public interface UserAchievementRepository
    extends JpaRepository<UserAchievement, UUID> {

    List<UserAchievement> findByUser_IdOrderByEarnedAtAsc(UUID userId);

    Optional<UserAchievement>
        findByUser_IdAndDefinition_AchievementCode(
            UUID userId,
            String achievementCode
        );
}

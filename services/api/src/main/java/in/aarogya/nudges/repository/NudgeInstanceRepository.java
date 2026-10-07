package in.aarogya.nudges.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.nudges.domain.NudgeInstance;

public interface NudgeInstanceRepository extends JpaRepository<NudgeInstance, UUID> {

    Optional<NudgeInstance> findByUser_IdAndNudgeKey(UUID userId, String nudgeKey);

    Optional<NudgeInstance> findByIdAndUser_Id(UUID id, UUID userId);

    List<NudgeInstance> findByUser_IdOrderByLastEvaluatedAtDesc(UUID userId);

    List<NudgeInstance> findByUser_IdAndStatusInOrderByLastEvaluatedAtDesc(
        UUID userId,
        List<String> statuses
    );
}

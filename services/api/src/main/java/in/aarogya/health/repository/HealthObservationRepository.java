package in.aarogya.health.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.health.domain.HealthObservation;

public interface HealthObservationRepository
    extends JpaRepository<HealthObservation, UUID> {

    long countByHealthRecord_User_Id(UUID userId);

    Optional<HealthObservation>
        findFirstByHealthRecord_User_IdAndHealthRecord_SourceTypeAndObservationCodeAndValueNumericIsNotNullOrderByObservedAtDesc(
            UUID userId,
            String sourceType,
            String observationCode
        );

    java.util.List<HealthObservation>
        findByHealthRecord_User_IdAndHealthRecord_SourceTypeAndObservationCodeAndValueNumericIsNotNullOrderByObservedAtAsc(
            UUID userId,
            String sourceType,
            String observationCode
        );
}

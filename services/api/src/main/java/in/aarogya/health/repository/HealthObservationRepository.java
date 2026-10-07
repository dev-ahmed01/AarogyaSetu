package in.aarogya.health.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.health.domain.HealthObservation;

public interface HealthObservationRepository
    extends JpaRepository<HealthObservation, UUID> {

    long countByHealthRecord_User_Id(UUID userId);
}

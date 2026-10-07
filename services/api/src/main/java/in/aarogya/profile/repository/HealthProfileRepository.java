package in.aarogya.profile.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.profile.domain.HealthProfile;

public interface HealthProfileRepository extends JpaRepository<HealthProfile, UUID> {

    Optional<HealthProfile> findByUserId(UUID userId);
}

package in.aarogya.health.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.health.domain.HealthIntegration;

public interface HealthIntegrationRepository
    extends JpaRepository<HealthIntegration, UUID> {

    Optional<HealthIntegration> findByUser_IdAndProviderCode(
        UUID userId,
        String providerCode
    );
}

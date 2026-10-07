package in.aarogya.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.identity.domain.RefreshSession;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {

    Optional<RefreshSession> findByTokenHashAndRevokedAtIsNull(String tokenHash);
}

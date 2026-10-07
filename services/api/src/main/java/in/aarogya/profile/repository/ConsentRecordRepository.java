package in.aarogya.profile.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.profile.domain.ConsentRecord;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {

    Optional<ConsentRecord> findTopByUser_IdAndConsentTypeOrderByRecordedAtDesc(
        UUID userId,
        String consentType
    );

    List<ConsentRecord> findByConsentTypeOrderByRecordedAtAsc(
        String consentType
    );
}

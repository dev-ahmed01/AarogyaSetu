package in.aarogya.health.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import in.aarogya.health.domain.HealthRecord;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, UUID> {

    @Query("""
        select record
        from HealthRecord record
        where record.user.id = :userId
          and (:recordType is null or record.recordType = :recordType)
          and (:fromDate is null or record.clinicalDate >= :fromDate)
          and (:toDate is null or record.clinicalDate <= :toDate)
        order by record.clinicalDate desc, record.createdAt desc
        """)
    List<HealthRecord> search(
        @Param("userId") UUID userId,
        @Param("recordType") String recordType,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    Optional<HealthRecord> findByIdAndUser_Id(UUID id, UUID userId);

    boolean existsByUser_IdAndSourceSystemAndSourceRecordRef(
        UUID userId,
        String sourceSystem,
        String sourceRecordRef
    );

    long countByUser_Id(UUID userId);

    Optional<HealthRecord> findTopByUser_IdOrderByClinicalDateDescCreatedAtDesc(
        UUID userId
    );
}

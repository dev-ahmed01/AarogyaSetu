package in.aarogya.research.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.research.domain.ResearchFeatureEvent;

public interface ResearchFeatureEventRepository
    extends JpaRepository<ResearchFeatureEvent, UUID> {

    boolean existsByUser_IdAndEventCodeAndEventDate(
        UUID userId,
        String eventCode,
        LocalDate eventDate
    );

    List<ResearchFeatureEvent> findByUser_IdInAndEventDateBetweenOrderByEventDateAsc(
        Collection<UUID> userIds,
        LocalDate from,
        LocalDate to
    );

    long deleteByUser_Id(UUID userId);
}

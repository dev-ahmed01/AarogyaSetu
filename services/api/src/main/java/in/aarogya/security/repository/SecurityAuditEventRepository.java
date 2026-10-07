package in.aarogya.security.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.security.domain.SecurityAuditEvent;

public interface SecurityAuditEventRepository
    extends JpaRepository<SecurityAuditEvent, Long> {

    List<SecurityAuditEvent> findByEventTypeOrderByOccurredAtDesc(
        String eventType,
        Pageable pageable
    );
}

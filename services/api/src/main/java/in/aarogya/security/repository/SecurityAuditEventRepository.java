package in.aarogya.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import in.aarogya.security.domain.SecurityAuditEvent;

public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, Long> {
}

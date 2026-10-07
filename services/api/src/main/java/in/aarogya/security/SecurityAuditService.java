package in.aarogya.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.security.domain.SecurityAuditEvent;
import in.aarogya.security.repository.SecurityAuditEventRepository;

@Service
public class SecurityAuditService {

    private final SecurityAuditEventRepository repository;

    public SecurityAuditService(SecurityAuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(
        UserAccount user,
        String eventType,
        String outcome,
        String subject,
        String metadataJson
    ) {
        repository.save(new SecurityAuditEvent(
            user,
            eventType,
            outcome,
            subject,
            metadataJson
        ));
    }
}

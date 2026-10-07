package in.aarogya.identity;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.domain.UserRole;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.identity.service.AccountDataService;
import in.aarogya.security.SecurityAuditService;

class AccountDataServiceTests {

    @Test
    void normalUserDeletionRequiresMatchingPasswordAndScrubsAuditMetadata() {
        var users = mock(UserAccountRepository.class);
        var encoder = mock(PasswordEncoder.class);
        var jdbc = mock(JdbcTemplate.class);
        var audit = mock(SecurityAuditService.class);
        var user = mock(UserAccount.class);
        var userId = UUID.randomUUID();

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(user.getRole()).thenReturn(UserRole.USER);
        when(user.getPasswordHash()).thenReturn("hash");
        when(user.getEmail()).thenReturn("user@example.test");
        when(encoder.matches("correct-password", "hash"))
            .thenReturn(true);

        var service = new AccountDataService(
            users,
            encoder,
            jdbc,
            audit
        );

        service.delete(userId, "correct-password");

        verify(jdbc).update(
            anyString(),
            eq(userId),
            eq("user@example.test"),
            anyString()
        );
        verify(users).delete(user);
        verify(users).flush();
    }

    @Test
    void staffAccountCannotSelfDelete() {
        var users = mock(UserAccountRepository.class);
        var encoder = mock(PasswordEncoder.class);
        var jdbc = mock(JdbcTemplate.class);
        var audit = mock(SecurityAuditService.class);
        var user = mock(UserAccount.class);
        var userId = UUID.randomUUID();

        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(user.getRole()).thenReturn(UserRole.ADMIN);

        var service = new AccountDataService(
            users,
            encoder,
            jdbc,
            audit
        );

        assertThrows(
            IllegalStateException.class,
            () -> service.delete(userId, "anything")
        );

        verify(jdbc, never()).update(
            anyString(),
            eq(userId),
            anyString(),
            anyString()
        );
        verify(users, never()).delete(user);
    }
}

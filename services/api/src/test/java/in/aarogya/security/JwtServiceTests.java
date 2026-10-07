package in.aarogya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import in.aarogya.identity.domain.UserAccount;

class JwtServiceTests {

    @Test
    void accessTokenContainsNoProfileOrRoleClaims() {
        var service = new JwtService(
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-extra-secret",
            15,
            30
        );
        var user = mock(UserAccount.class);
        var userId = UUID.randomUUID();

        when(user.getId()).thenReturn(userId);

        var token = service.issueAccessToken(user);
        var claims = service.parseAccessToken(token.value());

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(JwtService.ACCESS_TYPE, claims.get("type"));
        assertNull(claims.get("email"));
        assertNull(claims.get("name"));
        assertNull(claims.get("role"));
    }
}

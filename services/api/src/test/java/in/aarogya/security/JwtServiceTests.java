package in.aarogya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.domain.UserRole;

class JwtServiceTests {

    private static final String TEST_SECRET =
        "aarogya-test-secret-that-is-long-enough-for-hmac-sha256-2026";

    @Test
    void accessTokenContainsExpectedIdentityAndType() {
        var service = new JwtService(TEST_SECRET, 15, 30);
        var account = new UserAccount(
            "user@example.com",
            "hashed-password",
            "Test User",
            UserRole.USER
        );

        var token = service.issueAccessToken(account);
        var claims = service.parseAccessToken(token.value());

        assertEquals(account.getId(), service.subjectAsUserId(claims));
        assertEquals(JwtService.ACCESS_TYPE, claims.get("type", String.class));
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    void refreshTokensRotateAtTheTokenLevel() {
        var service = new JwtService(TEST_SECRET, 15, 30);
        var account = new UserAccount(
            "user@example.com",
            "hashed-password",
            "Test User",
            UserRole.USER
        );

        var first = service.issueRefreshToken(account);
        var second = service.issueRefreshToken(account);

        assertNotEquals(first.value(), second.value());
    }
}

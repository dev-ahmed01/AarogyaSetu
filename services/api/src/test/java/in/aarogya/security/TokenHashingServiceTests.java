package in.aarogya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class TokenHashingServiceTests {

    private final TokenHashingService service = new TokenHashingService();

    @Test
    void hashingIsDeterministicAndDoesNotStoreRawToken() {
        var token = "example-refresh-token";
        var first = service.sha256(token);
        var second = service.sha256(token);

        assertEquals(first, second);
        assertNotEquals(token, first);
        assertEquals(64, first.length());
    }
}

package in.aarogya.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AuthRateLimitFilterTests {

    @Test
    void authenticationRequestsAreRateLimitedPerRemoteAddress() throws Exception {
        var filter = new AuthRateLimitFilter(2, 300);
        var passed = new AtomicInteger();

        for (int attempt = 1; attempt <= 3; attempt++) {
            var request = new MockHttpServletRequest(
                "POST",
                "/api/auth/login"
            );
            request.setRemoteAddr("192.0.2.10");

            var response = new MockHttpServletResponse();

            filter.doFilter(
                request,
                response,
                (incomingRequest, outgoingResponse) ->
                    passed.incrementAndGet()
            );

            if (attempt < 3) {
                assertEquals(200, response.getStatus());
            } else {
                assertEquals(429, response.getStatus());
                assertEquals(
                    "2",
                    response.getHeader("X-RateLimit-Limit")
                );
            }
        }

        assertEquals(2, passed.get());
    }
}

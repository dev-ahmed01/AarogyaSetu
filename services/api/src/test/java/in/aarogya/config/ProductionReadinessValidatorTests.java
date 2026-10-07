package in.aarogya.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

class ProductionReadinessValidatorTests {

    @Test
    void productionRejectsDevelopmentSecuritySettings() {
        var validator = new ProductionReadinessValidator(
            "production",
            "short-secret",
            false,
            "None",
            "http://localhost:3000",
            "jdbc:postgresql://localhost:5432/aarogya",
            "aarogya_local"
        );

        assertThrows(
            IllegalStateException.class,
            () -> validator.run(mock(ApplicationArguments.class))
        );
    }

    @Test
    void productionAcceptsExplicitSecureSameSiteSettings() {
        var validator = new ProductionReadinessValidator(
            "production",
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-extra-secret",
            true,
            "Lax",
            "https://app.example.com",
            "jdbc:postgresql://db.internal:5432/aarogya",
            "non-default-production-password"
        );

        assertDoesNotThrow(
            () -> validator.run(mock(ApplicationArguments.class))
        );
    }
}

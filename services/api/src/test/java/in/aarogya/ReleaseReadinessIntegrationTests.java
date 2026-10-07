package in.aarogya;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.domain.UserRole;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.identity.service.AccountDataService;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(
    named = "AAROGYA_INTEGRATION_TESTS",
    matches = "true"
)
class ReleaseReadinessIntegrationTests {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserAccountRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountDataService accountDataService;

    @Test
    void allFlywayMigrationsReachReleaseSchema() {
        var applied = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version = '15'
              AND success = TRUE
            """,
            Integer.class
        );

        assertEquals(1, applied);
    }

    @Test
    void readinessProbeIsPublicAndHealthy() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
            .andExpect(status().isOk());
    }

    @Test
    void personalDataExportAndDeletionMatchTheRealSchema() {
        var password = "release-check-password";
        var user = userRepository.saveAndFlush(new UserAccount(
            "release-check-" + java.util.UUID.randomUUID() + "@example.test",
            passwordEncoder.encode(password),
            "Release Check",
            UserRole.USER
        ));

        var exported = accountDataService.export(user.getId());

        assertNotNull(exported.get("account"));
        assertNotNull(exported.get("meals"));
        assertNotNull(exported.get("healthRecords"));
        assertNotNull(exported.get("researchFeatureEvents"));

        accountDataService.delete(user.getId(), password);

        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    void publicApiResponseCarriesSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/status"))
            .andExpect(status().isOk())
            .andExpect(
                header().string(
                    "X-Content-Type-Options",
                    "nosniff"
                )
            )
            .andExpect(
                header().string(
                    "X-Frame-Options",
                    "DENY"
                )
            )
            .andExpect(
                header().string(
                    "Referrer-Policy",
                    "no-referrer"
                )
            )
            .andExpect(
                header().string(
                    "Cache-Control",
                    "no-store, no-cache, max-age=0, must-revalidate"
                )
            );
    }
}

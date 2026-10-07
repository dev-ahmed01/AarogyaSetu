package in.aarogya.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.domain.UserRole;
import in.aarogya.profile.domain.HealthProfile;

class HealthProfileTests {

    @Test
    void profileStoresNormalizedStructuredContext() {
        var user = new UserAccount(
            "profile@example.com",
            "hashed-password",
            "Profile User",
            UserRole.USER
        );

        var profile = new HealthProfile(user);
        profile.update(
            24,
            "PREFER_NOT_TO_SAY",
            new BigDecimal("172.50"),
            new BigDecimal("68.20"),
            "MODERATE",
            "VEGETARIAN",
            "Karnataka",
            Set.of("BALANCED_NUTRITION", "ENERGY"),
            Set.of("PEANUT"),
            Set.of("ANEMIA")
        );

        assertEquals(24, profile.getAgeYears());
        assertEquals("VEGETARIAN", profile.getDietaryPattern());
        assertEquals(Set.of("BALANCED_NUTRITION", "ENERGY"), profile.getGoals());
        assertEquals(Set.of("PEANUT"), profile.getAllergies());
        assertEquals(Set.of("ANEMIA"), profile.getHealthContexts());

        profile.markOnboardingComplete();
        assertNotNull(profile.getOnboardingCompletedAt());
    }
}

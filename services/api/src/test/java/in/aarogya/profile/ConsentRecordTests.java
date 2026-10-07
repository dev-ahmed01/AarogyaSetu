package in.aarogya.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.domain.UserRole;
import in.aarogya.profile.domain.ConsentRecord;

class ConsentRecordTests {

    @Test
    void consentIsAnAppendOnlyStyleSnapshot() {
        var user = new UserAccount(
            "consent@example.com",
            "hashed-password",
            "Consent User",
            UserRole.USER
        );

        var consent = new ConsentRecord(
            user,
            "HEALTH_PROFILE_PERSONALIZATION",
            true,
            "2026-10"
        );

        assertTrue(consent.isGranted());
        assertEquals("HEALTH_PROFILE_PERSONALIZATION", consent.getConsentType());
        assertEquals("2026-10", consent.getPolicyVersion());
        assertNotNull(consent.getRecordedAt());
    }
}

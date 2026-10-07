package in.aarogya.nudges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import in.aarogya.identity.domain.UserAccount;
import in.aarogya.nudges.domain.NudgeInstance;
import in.aarogya.nudges.domain.NudgeRule;

class NudgeInstanceTests {

    @Test
    void snoozeExpiresAndCooldownControlsReactivation() {
        var user = mock(UserAccount.class);
        var rule = mock(NudgeRule.class);

        when(rule.getRuleCode()).thenReturn("FIBRE_TREND_NUDGE");
        when(rule.getRuleVersion()).thenReturn(1);
        when(rule.getCategory()).thenReturn("NUTRITION");
        when(rule.getSeverity()).thenReturn("STANDARD");
        when(rule.getCooldownHours()).thenReturn(24);

        var nudge = new NudgeInstance(
            user,
            "RULE:FIBRE_TREND_NUDGE",
            rule,
            "Review fibre",
            "Example message",
            "Review",
            "/guidance",
            "FIBRE_BELOW_REFERENCE_TREND",
            "RECOMMENDATION",
            "FIBRE_TREND_LOW:v1",
            "Evidence",
            "https://example.test"
        );

        var now = Instant.parse("2026-10-07T08:00:00Z");
        nudge.snooze(now.plusSeconds(3600));
        assertEquals("SNOOZED", nudge.getStatus());

        nudge.refresh(
            rule,
            "Review fibre",
            "Example message",
            "Review",
            "/guidance",
            "FIBRE_BELOW_REFERENCE_TREND",
            "RECOMMENDATION",
            "FIBRE_TREND_LOW:v1",
            "Evidence",
            "https://example.test",
            now.plusSeconds(7200)
        );
        assertEquals("ACTIVE", nudge.getStatus());

        nudge.acknowledge(now.plusSeconds(7200));
        nudge.refresh(
            rule,
            "Review fibre",
            "Example message",
            "Review",
            "/guidance",
            "FIBRE_BELOW_REFERENCE_TREND",
            "RECOMMENDATION",
            "FIBRE_TREND_LOW:v1",
            "Evidence",
            "https://example.test",
            now.plusSeconds(10800)
        );
        assertEquals("ACKNOWLEDGED", nudge.getStatus());

        nudge.refresh(
            rule,
            "Review fibre",
            "Example message",
            "Review",
            "/guidance",
            "FIBRE_BELOW_REFERENCE_TREND",
            "RECOMMENDATION",
            "FIBRE_TREND_LOW:v1",
            "Evidence",
            "https://example.test",
            now.plusSeconds(28 * 3600L)
        );
        assertEquals("ACTIVE", nudge.getStatus());
    }
}

package in.aarogya.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import in.aarogya.progress.service.ProgressPolicy;

class ProgressPolicyTests {

    private final ProgressPolicy policy = new ProgressPolicy();

    @Test
    void currentRunSurvivesAnUnfinishedToday() {
        var today = LocalDate.of(2026, 10, 7);
        var metrics = policy.streaks(
            List.of(
                LocalDate.of(2026, 10, 4),
                LocalDate.of(2026, 10, 5),
                LocalDate.of(2026, 10, 6)
            ),
            today
        );

        assertEquals(3, metrics.currentRunDays());
        assertEquals(3, metrics.longestRunDays());
        assertFalse(metrics.todayLogged());
        assertEquals(LocalDate.of(2026, 10, 6), metrics.lastLoggedDate());
    }

    @Test
    void gapEndsCurrentRunButPreservesLongestRun() {
        var today = LocalDate.of(2026, 10, 7);
        var metrics = policy.streaks(
            List.of(
                LocalDate.of(2026, 9, 28),
                LocalDate.of(2026, 9, 29),
                LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 10, 5)
            ),
            today
        );

        assertEquals(0, metrics.currentRunDays());
        assertEquals(3, metrics.longestRunDays());
        assertEquals(4, metrics.totalLoggingDays());
    }

    @Test
    void weeklyProgressCountsDistinctLoggingDaysOnly() {
        var today = LocalDate.of(2026, 10, 7);
        var week = policy.week(today);

        assertEquals(LocalDate.of(2026, 10, 5), week.start());
        assertEquals(LocalDate.of(2026, 10, 11), week.end());
        assertEquals(
            2,
            policy.loggingDaysInWindow(
                List.of(
                    LocalDate.of(2026, 10, 5),
                    LocalDate.of(2026, 10, 5),
                    LocalDate.of(2026, 10, 6),
                    LocalDate.of(2026, 10, 3)
                ),
                week.start(),
                week.end()
            )
        );
        assertEquals(50, policy.progressPercent(2, 4));
        assertEquals(100, policy.progressPercent(6, 4));
    }
}

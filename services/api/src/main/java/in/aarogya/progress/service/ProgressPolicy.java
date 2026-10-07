package in.aarogya.progress.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ProgressPolicy {

    public WeekWindow week(LocalDate today) {
        var start = today.with(
            TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );
        return new WeekWindow(start, start.plusDays(6));
    }

    public int loggingDaysInWindow(
        List<LocalDate> loggingDates,
        LocalDate from,
        LocalDate to
    ) {
        return (int) loggingDates.stream()
            .distinct()
            .filter(date -> !date.isBefore(from) && !date.isAfter(to))
            .count();
    }

    public StreakMetrics streaks(
        List<LocalDate> loggingDates,
        LocalDate today
    ) {
        var unique = new HashSet<>(loggingDates);

        if (unique.isEmpty()) {
            return new StreakMetrics(0, 0, 0, false, null);
        }

        var sorted = unique.stream().sorted().toList();
        var longest = 1;
        var running = 1;

        for (var index = 1; index < sorted.size(); index++) {
            if (sorted.get(index - 1).plusDays(1).equals(sorted.get(index))) {
                running++;
                longest = Math.max(longest, running);
            } else {
                running = 1;
            }
        }

        var todayLogged = unique.contains(today);
        var anchor = todayLogged
            ? today
            : unique.contains(today.minusDays(1))
                ? today.minusDays(1)
                : null;
        var current = 0;

        if (anchor != null) {
            var cursor = anchor;
            while (unique.contains(cursor)) {
                current++;
                cursor = cursor.minusDays(1);
            }
        }

        return new StreakMetrics(
            current,
            longest,
            unique.size(),
            todayLogged,
            sorted.get(sorted.size() - 1)
        );
    }

    public int progressPercent(int current, int target) {
        if (target <= 0) return 0;
        return Math.min(
            100,
            (int) Math.round((current * 100.0) / target)
        );
    }

    public record WeekWindow(
        LocalDate start,
        LocalDate end
    ) {}

    public record StreakMetrics(
        int currentRunDays,
        int longestRunDays,
        int totalLoggingDays,
        boolean todayLogged,
        LocalDate lastLoggedDate
    ) {}
}

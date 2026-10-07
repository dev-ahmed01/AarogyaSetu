package in.aarogya.progress.api;

import java.time.LocalDate;

public record StreakResponse(
    int currentRunDays,
    int longestRunDays,
    int totalLoggingDays,
    boolean todayLogged,
    LocalDate lastLoggedDate,
    String message
) {
}

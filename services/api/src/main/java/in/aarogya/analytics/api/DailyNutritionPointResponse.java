package in.aarogya.analytics.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyNutritionPointResponse(
    LocalDate date,
    boolean logged,
    int entryCount,
    BigDecimal energyKcal,
    BigDecimal proteinG,
    BigDecimal fibreG
) {
}

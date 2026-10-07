package in.aarogya.analytics.api;

import java.math.BigDecimal;

public record NutrientTrendResponse(
    String nutrientCode,
    String label,
    String unit,
    BigDecimal currentLoggedDayAverage,
    BigDecimal previousLoggedDayAverage,
    BigDecimal changePercent,
    int currentObservedDays,
    int previousObservedDays,
    String direction,
    String interpretation
) {
}

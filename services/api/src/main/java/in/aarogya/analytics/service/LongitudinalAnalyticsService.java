package in.aarogya.analytics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.analytics.api.AnalyticsCoverageResponse;
import in.aarogya.analytics.api.DailyNutritionPointResponse;
import in.aarogya.analytics.api.HealthTrendPointResponse;
import in.aarogya.analytics.api.HealthTrendResponse;
import in.aarogya.analytics.api.LongitudinalAnalyticsResponse;
import in.aarogya.analytics.api.MealPatternResponse;
import in.aarogya.analytics.api.NutrientTrendResponse;
import in.aarogya.health.repository.HealthObservationRepository;
import in.aarogya.health.service.HealthRecordService;
import in.aarogya.meals.domain.MealEntry;
import in.aarogya.meals.repository.MealEntryRepository;

@Service
public class LongitudinalAnalyticsService {

    private static final List<NutrientSpec> NUTRIENTS = List.of(
        new NutrientSpec("ENERGY_KCAL", "Energy", "kcal"),
        new NutrientSpec("PROTEIN_G", "Protein", "g"),
        new NutrientSpec("FIBRE_G", "Fibre", "g")
    );

    private static final List<String> MEAL_TYPES = List.of(
        "BREAKFAST", "LUNCH", "DINNER", "SNACK"
    );

    private final MealEntryRepository mealRepository;
    private final HealthObservationRepository healthObservationRepository;
    private final HealthRecordService healthRecordService;

    public LongitudinalAnalyticsService(
        MealEntryRepository mealRepository,
        HealthObservationRepository healthObservationRepository,
        HealthRecordService healthRecordService
    ) {
        this.mealRepository = mealRepository;
        this.healthObservationRepository = healthObservationRepository;
        this.healthRecordService = healthRecordService;
    }

    @Transactional(readOnly = true)
    public LongitudinalAnalyticsResponse analyze(
        UUID userId,
        LocalDate requestedDate,
        int requestedWindowDays
    ) {
        var asOf = requestedDate == null ? LocalDate.now() : requestedDate;

        if (asOf.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Analytics date cannot be in the future."
            );
        }

        var windowDays = validateWindow(requestedWindowDays);
        var currentFrom = asOf.minusDays(windowDays - 1L);
        var previousTo = currentFrom.minusDays(1);
        var previousFrom = previousTo.minusDays(windowDays - 1L);

        var entries = mealRepository
            .findByUser_IdAndMealDateBetweenOrderByMealDateDescCreatedAtAsc(
                userId,
                previousFrom,
                asOf
            );

        var currentEntries = entries.stream()
            .filter(entry -> !entry.getMealDate().isBefore(currentFrom))
            .toList();
        var previousEntries = entries.stream()
            .filter(entry -> entry.getMealDate().isBefore(currentFrom))
            .toList();

        var currentDays = aggregateByDay(currentEntries);
        var previousDays = aggregateByDay(previousEntries);

        var coverage = coverage(windowDays, currentDays.size());
        var previousCoverage = coverage(windowDays, previousDays.size());

        var dailyNutrition = dailySeries(currentFrom, asOf, currentDays);
        var nutrientTrends = NUTRIENTS.stream()
            .map(spec -> nutrientTrend(
                spec,
                currentDays,
                previousDays,
                windowDays
            ))
            .toList();
        var mealPatterns = mealPatterns(currentEntries);
        var healthTrend = healthTrend(userId, asOf);
        var insights = insights(
            coverage,
            previousCoverage,
            nutrientTrends,
            mealPatterns
        );

        var notices = new ArrayList<String>();
        notices.add(
            "Unlogged days are treated as missing data and are excluded from logged-day nutrient averages."
        );
        notices.add(
            "These analytics describe recorded data only; they do not estimate food that was not logged."
        );

        if (coverage.loggedDays() < 2) {
            notices.add(
                "At least two logged days are needed before nutrient-window comparisons become descriptive."
            );
        }

        if (!healthTrend.analysisConsentGranted()) {
            notices.add(
                "Health-record trend analysis is paused because HEALTH_RECORD_ANALYSIS consent is not granted."
            );
        } else if ("NO_DATA".equals(healthTrend.status())) {
            notices.add(
                "No eligible manual body-weight observations are available for the health trend."
            );
        }

        return new LongitudinalAnalyticsResponse(
            asOf,
            windowDays,
            currentFrom,
            asOf,
            previousFrom,
            previousTo,
            coverage,
            previousCoverage,
            currentEntries.size(),
            previousEntries.size(),
            dailyNutrition,
            nutrientTrends,
            mealPatterns,
            healthTrend,
            List.copyOf(insights),
            List.copyOf(notices)
        );
    }

    private int validateWindow(int windowDays) {
        if (windowDays != 7 && windowDays != 30) {
            throw new IllegalArgumentException(
                "Analytics window must be either 7 or 30 days."
            );
        }
        return windowDays;
    }

    private Map<LocalDate, DayAggregate> aggregateByDay(
        List<MealEntry> entries
    ) {
        var result = new LinkedHashMap<LocalDate, DayAggregate>();

        for (var entry : entries) {
            var day = result.computeIfAbsent(
                entry.getMealDate(),
                ignored -> new DayAggregate()
            );
            day.entryCount++;
            day.mealTypes.add(entry.getMealType());

            for (var nutrient : entry.getNutrientSnapshots()) {
                day.nutrients.merge(
                    nutrient.getNutrientCode(),
                    nutrient.getAmount(),
                    BigDecimal::add
                );
            }
        }

        return result;
    }

    private AnalyticsCoverageResponse coverage(
        int windowDays,
        int loggedDays
    ) {
        var percent = (int) Math.round(
            (loggedDays * 100.0) / windowDays
        );

        var status = loggedDays == 0
            ? "EMPTY"
            : loggedDays < 2
                ? "SPARSE"
                : percent < 50
                    ? "LIMITED"
                    : "USABLE";

        var label = switch (status) {
            case "EMPTY" -> "No logged days";
            case "SPARSE" -> "Too little data for comparison";
            case "LIMITED" -> "Partial coverage";
            default -> "Usable coverage";
        };

        return new AnalyticsCoverageResponse(
            windowDays,
            loggedDays,
            percent,
            status,
            label
        );
    }

    private List<DailyNutritionPointResponse> dailySeries(
        LocalDate from,
        LocalDate to,
        Map<LocalDate, DayAggregate> days
    ) {
        var points = new ArrayList<DailyNutritionPointResponse>();

        for (var date = from; !date.isAfter(to); date = date.plusDays(1)) {
            var day = days.get(date);
            points.add(new DailyNutritionPointResponse(
                date,
                day != null,
                day == null ? 0 : day.entryCount,
                nutrient(day, "ENERGY_KCAL"),
                nutrient(day, "PROTEIN_G"),
                nutrient(day, "FIBRE_G")
            ));
        }

        return List.copyOf(points);
    }

    private NutrientTrendResponse nutrientTrend(
        NutrientSpec spec,
        Map<LocalDate, DayAggregate> currentDays,
        Map<LocalDate, DayAggregate> previousDays,
        int windowDays
    ) {
        var currentValues = values(currentDays, spec.code());
        var previousValues = values(previousDays, spec.code());
        var currentAverage = average(currentValues);
        var previousAverage = average(previousValues);

        BigDecimal change = null;
        String direction = "INSUFFICIENT_DATA";
        String interpretation =
            "More logged days are needed for a window-to-window comparison.";

        if (currentValues.size() >= 2
            && previousValues.size() >= 2
            && currentAverage != null
            && previousAverage != null
            && previousAverage.compareTo(BigDecimal.ZERO) > 0) {
            change = currentAverage
                .subtract(previousAverage)
                .divide(previousAverage, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);

            if (change.abs().compareTo(new BigDecimal("5.0")) < 0) {
                direction = "SIMILAR";
                interpretation =
                    "The logged-day average is within 5% of the previous "
                        + windowDays
                        + "-day window.";
            } else if (change.compareTo(BigDecimal.ZERO) > 0) {
                direction = "HIGHER";
                interpretation =
                    "The logged-day average is "
                        + change.abs().stripTrailingZeros().toPlainString()
                        + "% higher than the previous "
                        + windowDays
                        + "-day window.";
            } else {
                direction = "LOWER";
                interpretation =
                    "The logged-day average is "
                        + change.abs().stripTrailingZeros().toPlainString()
                        + "% lower than the previous "
                        + windowDays
                        + "-day window.";
            }
        }

        return new NutrientTrendResponse(
            spec.code(),
            spec.label(),
            spec.unit(),
            currentAverage,
            previousAverage,
            change,
            currentValues.size(),
            previousValues.size(),
            direction,
            interpretation
        );
    }

    private List<MealPatternResponse> mealPatterns(
        List<MealEntry> entries
    ) {
        var result = new ArrayList<MealPatternResponse>();

        for (var mealType : MEAL_TYPES) {
            var matching = entries.stream()
                .filter(entry -> mealType.equals(entry.getMealType()))
                .toList();
            var daysPresent = matching.stream()
                .map(MealEntry::getMealDate)
                .distinct()
                .count();

            result.add(new MealPatternResponse(
                mealType,
                matching.size(),
                (int) daysPresent
            ));
        }

        return List.copyOf(result);
    }

    private HealthTrendResponse healthTrend(
        UUID userId,
        LocalDate asOf
    ) {
        var consent = healthRecordService.consent(userId);

        if (!consent.granted()) {
            return new HealthTrendResponse(
                false,
                "BODY_WEIGHT",
                "Self-reported body weight",
                "MANUAL",
                "CONSENT_REQUIRED",
                "Grant health-record analysis consent before Aarogya computes a longitudinal health series.",
                List.of()
            );
        }

        var cutoff = asOf.minusDays(365)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC);

        var eligible = healthObservationRepository
            .findByHealthRecord_User_IdAndHealthRecord_SourceTypeAndObservationCodeAndValueNumericIsNotNullOrderByObservedAtAsc(
                userId,
                "MANUAL",
                "BODY_WEIGHT"
            )
            .stream()
            .filter(item -> item.getObservedAt() != null)
            .filter(item -> !item.getObservedAt().isBefore(cutoff))
            .filter(item -> item.getUnit() != null)
            .filter(item -> "kg".equalsIgnoreCase(item.getUnit().trim()))
            .toList();

        var selected = eligible.size() <= 24
            ? eligible
            : eligible.subList(eligible.size() - 24, eligible.size());

        if (selected.isEmpty()) {
            return new HealthTrendResponse(
                true,
                "BODY_WEIGHT",
                "Self-reported body weight",
                "MANUAL",
                "NO_DATA",
                "No manual body-weight observations with kg units are available in the last 365 days.",
                List.of()
            );
        }

        var points = selected.stream()
            .map(item -> new HealthTrendPointResponse(
                item.getObservedAt(),
                item.getValueNumeric().setScale(2, RoundingMode.HALF_UP),
                item.getUnit()
            ))
            .toList();

        return new HealthTrendResponse(
            true,
            "BODY_WEIGHT",
            "Self-reported body weight",
            "MANUAL",
            "AVAILABLE",
            "This series shows self-reported manual records only. Aarogya does not interpret change as healthy, unhealthy, or diagnostic.",
            points
        );
    }

    private List<String> insights(
        AnalyticsCoverageResponse currentCoverage,
        AnalyticsCoverageResponse previousCoverage,
        List<NutrientTrendResponse> trends,
        List<MealPatternResponse> mealPatterns
    ) {
        var insights = new ArrayList<String>();

        insights.add(
            "Meals were recorded on "
                + currentCoverage.loggedDays()
                + " of "
                + currentCoverage.windowDays()
                + " days in the current window."
        );

        if (previousCoverage.loggedDays() > 0) {
            var delta = currentCoverage.loggedDays()
                - previousCoverage.loggedDays();

            if (delta > 0) {
                insights.add(
                    "The current window contains "
                        + delta
                        + " more logged "
                        + (delta == 1 ? "day" : "days")
                        + " than the previous window."
                );
            } else if (delta < 0) {
                insights.add(
                    "The current window contains "
                        + Math.abs(delta)
                        + " fewer logged "
                        + (Math.abs(delta) == 1 ? "day" : "days")
                        + " than the previous window, so comparisons should be read cautiously."
                );
            }
        }

        trends.stream()
            .filter(item -> !"INSUFFICIENT_DATA".equals(item.direction()))
            .limit(2)
            .forEach(item ->
                insights.add(item.label() + ": " + item.interpretation())
            );

        mealPatterns.stream()
            .max(Comparator.comparingInt(MealPatternResponse::daysPresent))
            .filter(item -> item.daysPresent() > 0)
            .ifPresent(item ->
                insights.add(
                    prettyMeal(item.mealType())
                        + " appears on "
                        + item.daysPresent()
                        + " logged "
                        + (item.daysPresent() == 1 ? "day" : "days")
                        + " in the current window."
                )
            );

        return List.copyOf(insights);
    }

    private List<BigDecimal> values(
        Map<LocalDate, DayAggregate> days,
        String nutrientCode
    ) {
        return days.values().stream()
            .map(day -> day.nutrients.get(nutrientCode))
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) return null;

        var sum = values.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(
            BigDecimal.valueOf(values.size()),
            2,
            RoundingMode.HALF_UP
        );
    }

    private BigDecimal nutrient(
        DayAggregate day,
        String code
    ) {
        if (day == null) return null;
        var value = day.nutrients.get(code);
        return value == null
            ? null
            : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String prettyMeal(String mealType) {
        return mealType.charAt(0)
            + mealType.substring(1).toLowerCase(java.util.Locale.ROOT);
    }

    private static class DayAggregate {
        private int entryCount = 0;
        private final Map<String, BigDecimal> nutrients =
            new LinkedHashMap<>();
        private final Set<String> mealTypes = new LinkedHashSet<>();
    }

    private record NutrientSpec(
        String code,
        String label,
        String unit
    ) {}
}

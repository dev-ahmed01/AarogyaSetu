package in.aarogya.meals.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.meals.api.DailyMealLogResponse;
import in.aarogya.meals.api.MealEntryResponse;
import in.aarogya.meals.api.MealEntryUpsertRequest;
import in.aarogya.meals.api.MealHistoryResponse;
import in.aarogya.meals.api.NutrientTotalResponse;
import in.aarogya.meals.domain.MealEntry;
import in.aarogya.meals.domain.UserFoodFavorite;
import in.aarogya.meals.repository.MealEntryRepository;
import in.aarogya.meals.repository.UserFoodFavoriteRepository;
import in.aarogya.nutrition.api.FoodSummaryResponse;
import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodPortion;
import in.aarogya.nutrition.repository.FoodRepository;

@Service
public class MealLogService {

    private static final String LOGGABLE_STATUS = "SOURCE_REFERENCED";
    private static final List<String> MEAL_TYPES = List.of(
        "BREAKFAST", "LUNCH", "DINNER", "SNACK"
    );

    private final MealEntryRepository mealRepository;
    private final UserFoodFavoriteRepository favoriteRepository;
    private final UserAccountRepository userRepository;
    private final FoodRepository foodRepository;

    public MealLogService(
        MealEntryRepository mealRepository,
        UserFoodFavoriteRepository favoriteRepository,
        UserAccountRepository userRepository,
        FoodRepository foodRepository
    ) {
        this.mealRepository = mealRepository;
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
    }

    @Transactional(readOnly = true)
    public DailyMealLogResponse day(UUID userId, LocalDate date) {
        var targetDate = date == null ? LocalDate.now() : date;
        var entries = mealRepository
            .findByUser_IdAndMealDateOrderByCreatedAtAsc(userId, targetDate);

        return toDailyResponse(targetDate, entries);
    }

    @Transactional(readOnly = true)
    public MealHistoryResponse history(
        UUID userId,
        LocalDate from,
        LocalDate to
    ) {
        var end = to == null ? LocalDate.now() : to;
        var start = from == null ? end.minusDays(6) : from;

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("History start date must be on or before end date.");
        }

        if (start.plusDays(30).isBefore(end)) {
            throw new IllegalArgumentException("Meal history requests are limited to 31 days.");
        }

        var entries = mealRepository
            .findByUser_IdAndMealDateBetweenOrderByMealDateDescCreatedAtAsc(
                userId,
                start,
                end
            );

        var byDate = new LinkedHashMap<LocalDate, List<MealEntry>>();

        for (var date = end; !date.isBefore(start); date = date.minusDays(1)) {
            byDate.put(date, new ArrayList<>());
        }

        for (var entry : entries) {
            byDate.computeIfAbsent(entry.getMealDate(), ignored -> new ArrayList<>())
                .add(entry);
        }

        var days = byDate.entrySet().stream()
            .map(item -> toDailyResponse(item.getKey(), item.getValue()))
            .toList();

        return new MealHistoryResponse(start, end, days);
    }

    @Transactional
    public MealEntryResponse create(
        UUID userId,
        MealEntryUpsertRequest request
    ) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        var entry = new MealEntry(user);
        applyRequest(entry, request);

        return MealEntryResponse.from(mealRepository.save(entry));
    }

    @Transactional
    public MealEntryResponse update(
        UUID userId,
        UUID entryId,
        MealEntryUpsertRequest request
    ) {
        var entry = mealRepository.findByIdAndUser_Id(entryId, userId)
            .orElseThrow(MealEntryNotFoundException::new);

        applyRequest(entry, request);
        return MealEntryResponse.from(entry);
    }

    @Transactional
    public void delete(UUID userId, UUID entryId) {
        var entry = mealRepository.findByIdAndUser_Id(entryId, userId)
            .orElseThrow(MealEntryNotFoundException::new);

        mealRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<FoodSummaryResponse> recentFoods(UUID userId, int limit) {
        var safeLimit = Math.min(Math.max(limit, 1), 20);
        var candidates = mealRepository.findByUser_IdOrderByCreatedAtDesc(
            userId,
            PageRequest.of(0, Math.min(safeLimit * 8, 100))
        );

        var unique = new LinkedHashMap<String, FoodSummaryResponse>();

        for (var entry : candidates) {
            var food = entry.getFood();
            if (!isLoggable(food)) {
                continue;
            }

            unique.putIfAbsent(food.getSlug(), FoodSummaryResponse.from(food));

            if (unique.size() >= safeLimit) {
                break;
            }
        }

        return List.copyOf(unique.values());
    }

    @Transactional(readOnly = true)
    public List<FoodSummaryResponse> favorites(UUID userId) {
        return favoriteRepository.findByUser_IdOrderByCreatedAtDesc(userId)
            .stream()
            .map(UserFoodFavorite::getFood)
            .filter(this::isLoggable)
            .map(FoodSummaryResponse::from)
            .toList();
    }

    @Transactional
    public FoodSummaryResponse addFavorite(UUID userId, String foodSlug) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        var food = requireLoggableFood(foodSlug);

        favoriteRepository.findByUser_IdAndFood_Id(userId, food.getId())
            .orElseGet(() -> favoriteRepository.save(new UserFoodFavorite(user, food)));

        return FoodSummaryResponse.from(food);
    }

    @Transactional
    public void removeFavorite(UUID userId, String foodSlug) {
        var food = foodRepository.findBySlugAndActiveTrue(foodSlug)
            .orElse(null);

        if (food == null) {
            return;
        }

        favoriteRepository.findByUser_IdAndFood_Id(userId, food.getId())
            .ifPresent(favoriteRepository::delete);
    }

    private void applyRequest(
        MealEntry entry,
        MealEntryUpsertRequest request
    ) {
        var food = requireLoggableFood(request.foodSlug());
        var mealType = normalizeMealType(request.mealType());
        var date = request.mealDate();

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Meal logs cannot be dated in the future.");
        }

        var resolved = resolveQuantity(food, request);

        var sourceCode = food.getSource() == null
            ? null
            : food.getSource().getSourceCode();

        entry.replaceSnapshot(
            food,
            resolved.portion(),
            date,
            mealType,
            resolved.grams(),
            resolved.portionCount(),
            resolved.portionLabel(),
            sourceCode,
            food.getSourceFoodRef()
        );
    }

    private Food requireLoggableFood(String foodSlug) {
        var food = foodRepository.findBySlugAndActiveTrue(foodSlug.trim())
            .orElseThrow(() -> new IllegalArgumentException("Food record was not found."));

        if (!isLoggable(food)) {
            throw new FoodNotLoggableException(food.getCanonicalName());
        }

        return food;
    }

    private boolean isLoggable(Food food) {
        return LOGGABLE_STATUS.equals(food.getNutrientStatus())
            && !food.getNutrients().isEmpty();
    }

    private QuantityResolution resolveQuantity(
        Food food,
        MealEntryUpsertRequest request
    ) {
        if (request.portionId() != null) {
            if (request.grams() != null) {
                throw new IllegalArgumentException(
                    "Choose either a catalog portion or custom grams, not both."
                );
            }

            var portion = food.getPortions().stream()
                .filter(item -> item.getId().equals(request.portionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "The selected portion does not belong to this food."
                ));

            var count = request.portionCount() == null
                ? BigDecimal.ONE
                : request.portionCount();

            var grams = portion.getGrams()
                .multiply(count)
                .setScale(2, RoundingMode.HALF_UP);

            validateQuantity(grams);

            return new QuantityResolution(
                portion,
                grams,
                count.setScale(3, RoundingMode.HALF_UP),
                portion.getLabel()
            );
        }

        if (request.portionCount() != null) {
            throw new IllegalArgumentException(
                "Portion count requires a catalog portion."
            );
        }

        if (request.grams() == null) {
            throw new IllegalArgumentException(
                "Provide a catalog portion or a custom gram quantity."
            );
        }

        var grams = request.grams().setScale(2, RoundingMode.HALF_UP);
        validateQuantity(grams);

        return new QuantityResolution(
            null,
            grams,
            null,
            "Custom amount"
        );
    }

    private void validateQuantity(BigDecimal grams) {
        if (grams.compareTo(BigDecimal.ONE) < 0
            || grams.compareTo(new BigDecimal("5000")) > 0) {
            throw new IllegalArgumentException(
                "Meal quantity must be between 1g and 5000g."
            );
        }
    }

    private String normalizeMealType(String mealType) {
        var normalized = mealType.trim().toUpperCase(Locale.ROOT);

        if (!MEAL_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported meal type.");
        }

        return normalized;
    }

    private DailyMealLogResponse toDailyResponse(
        LocalDate date,
        List<MealEntry> entries
    ) {
        var totals = new LinkedHashMap<String, Aggregate>();

        for (var entry : entries) {
            for (var nutrient : entry.getNutrientSnapshots()) {
                totals.compute(nutrient.getNutrientCode(), (code, current) -> {
                    if (current == null) {
                        return new Aggregate(
                            nutrient.getAmount(),
                            nutrient.getUnit()
                        );
                    }

                    if (!current.unit().equals(nutrient.getUnit())) {
                        throw new IllegalStateException(
                            "Nutrient unit mismatch for " + code
                        );
                    }

                    return new Aggregate(
                        current.amount().add(nutrient.getAmount()),
                        current.unit()
                    );
                });
            }
        }

        var totalResponses = totals.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(item -> new NutrientTotalResponse(
                item.getKey(),
                item.getValue().amount().setScale(2, RoundingMode.HALF_UP),
                item.getValue().unit()
            ))
            .toList();

        var entryResponses = entries.stream()
            .sorted(
                Comparator.comparingInt(
                    entry -> MEAL_TYPES.indexOf(entry.getMealType())
                ).thenComparing(MealEntry::getCreatedAt)
            )
            .map(MealEntryResponse::from)
            .toList();

        return new DailyMealLogResponse(
            date,
            entryResponses.size(),
            entryResponses,
            totalResponses
        );
    }

    private record QuantityResolution(
        FoodPortion portion,
        BigDecimal grams,
        BigDecimal portionCount,
        String portionLabel
    ) {
    }

    private record Aggregate(
        BigDecimal amount,
        String unit
    ) {
    }
}

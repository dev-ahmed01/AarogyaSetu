package in.aarogya.administration.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.administration.api.AdminFoodDetailResponse;
import in.aarogya.administration.api.AdminFoodPageResponse;
import in.aarogya.administration.api.AdminFoodSummaryResponse;
import in.aarogya.administration.api.AdminNutrientResponse;
import in.aarogya.administration.api.AdminOverviewResponse;
import in.aarogya.administration.api.AdminPortionResponse;
import in.aarogya.administration.api.AdminReviewResponse;
import in.aarogya.administration.api.AdminSourceResponse;
import in.aarogya.administration.api.AuditEventResponse;
import in.aarogya.administration.api.CreateNutritionSourceRequest;
import in.aarogya.administration.api.CuratedNutrientRequest;
import in.aarogya.administration.api.FoodCurationRequest;
import in.aarogya.administration.domain.FoodCurationReview;
import in.aarogya.administration.repository.FoodCurationReviewRepository;
import in.aarogya.health.repository.HealthRecordRepository;
import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.meals.repository.MealEntryRepository;
import in.aarogya.nutrition.domain.Food;
import in.aarogya.nutrition.domain.FoodNutrient;
import in.aarogya.nutrition.domain.FoodPortion;
import in.aarogya.nutrition.domain.NutritionSource;
import in.aarogya.nutrition.repository.FoodNutrientRepository;
import in.aarogya.nutrition.repository.FoodPortionRepository;
import in.aarogya.nutrition.repository.FoodRepository;
import in.aarogya.nutrition.repository.NutritionSourceRepository;
import in.aarogya.security.AarogyaPrincipal;
import in.aarogya.security.SecurityAuditService;
import in.aarogya.security.repository.SecurityAuditEventRepository;

@Service
public class AdministrationService {

    private static final Set<String> CURATION_STATUSES = Set.of(
        "NEEDS_REVIEW",
        "IN_REVIEW",
        "READY_TO_PUBLISH",
        "PUBLISHED",
        "UNPUBLISHED"
    );

    private static final Set<String> SOURCE_TYPES = Set.of(
        "FOOD_COMPOSITION",
        "EDITORIAL",
        "GUIDANCE_REFERENCE",
        "REGULATORY_REFERENCE"
    );

    private static final Map<String, String> REQUIRED_NUTRIENTS =
        Map.of(
            "ENERGY_KCAL", "kcal",
            "PROTEIN_G", "g",
            "CARBOHYDRATE_G", "g",
            "FAT_G", "g",
            "FIBRE_G", "g"
        );

    private final FoodRepository foodRepository;
    private final FoodNutrientRepository nutrientRepository;
    private final FoodPortionRepository portionRepository;
    private final NutritionSourceRepository sourceRepository;
    private final FoodCurationReviewRepository reviewRepository;
    private final UserAccountRepository userRepository;
    private final MealEntryRepository mealRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final SecurityAuditEventRepository auditRepository;
    private final SecurityAuditService auditService;

    public AdministrationService(
        FoodRepository foodRepository,
        FoodNutrientRepository nutrientRepository,
        FoodPortionRepository portionRepository,
        NutritionSourceRepository sourceRepository,
        FoodCurationReviewRepository reviewRepository,
        UserAccountRepository userRepository,
        MealEntryRepository mealRepository,
        HealthRecordRepository healthRecordRepository,
        SecurityAuditEventRepository auditRepository,
        SecurityAuditService auditService
    ) {
        this.foodRepository = foodRepository;
        this.nutrientRepository = nutrientRepository;
        this.portionRepository = portionRepository;
        this.sourceRepository = sourceRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.mealRepository = mealRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.auditRepository = auditRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse overview(AarogyaPrincipal principal) {
        var admin = "ADMIN".equals(principal.role());

        return new AdminOverviewResponse(
            principal.role(),
            foodRepository.count(),
            foodRepository.countByCurationStatus("NEEDS_REVIEW"),
            foodRepository.countByCurationStatus("IN_REVIEW"),
            foodRepository.countByCurationStatus("READY_TO_PUBLISH"),
            foodRepository.countByCurationStatus("PUBLISHED"),
            foodRepository.countByCurationStatus("UNPUBLISHED"),
            sourceRepository.count(),
            admin ? userRepository.count() : null,
            admin ? mealRepository.count() : null,
            admin ? healthRecordRepository.count() : null,
            admin ? auditRepository.count() : null
        );
    }

    @Transactional(readOnly = true)
    public AdminFoodPageResponse foods(
        String query,
        String curationStatus,
        int page,
        int size
    ) {
        var safePage = Math.max(page, 0);
        var safeSize = Math.min(Math.max(size, 1), 30);
        var normalizedStatus = normalizeStatus(curationStatus);

        var result = foodRepository.searchForCuration(
            normalizeOptional(query),
            normalizedStatus,
            PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.ASC, "canonicalName")
            )
        );

        var items = result.getContent().stream()
            .map(this::summary)
            .toList();

        return new AdminFoodPageResponse(
            items,
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AdminFoodDetailResponse food(UUID foodId) {
        var food = requireFood(foodId);
        return detail(food);
    }

    @Transactional
    public AdminFoodDetailResponse saveCuration(
        AarogyaPrincipal principal,
        UUID foodId,
        FoodCurationRequest request
    ) {
        var food = requireFood(foodId);

        if ("PUBLISHED".equals(food.getCurationStatus())) {
            throw new IllegalStateException(
                "Unpublish this food before replacing its curated nutrition data."
            );
        }

        var source = sourceRepository.findById(request.sourceId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Nutrition source was not found."
            ));

        if (!"FOOD_COMPOSITION".equals(source.getSourceType())) {
            throw new IllegalArgumentException(
                "Publishable nutrient data must use a FOOD_COMPOSITION source."
            );
        }

        var sourceFoodRef = request.sourceFoodRef().trim();
        var nutrients = validateNutrients(request.nutrients());
        var reviewer = requireReviewer(principal.id());
        var fromStatus = food.getCurationStatus();

        nutrientRepository.deleteByFood_Id(foodId);
        portionRepository.deleteByFood_Id(foodId);

        food.applyCuratedNutrition(source, sourceFoodRef);
        foodRepository.saveAndFlush(food);

        var nutrientEntities = nutrients.values().stream()
            .map(item -> new FoodNutrient(
                food,
                item.code(),
                item.amountPer100g().setScale(4, RoundingMode.HALF_UP),
                item.unit(),
                source,
                sourceFoodRef
            ))
            .toList();

        nutrientRepository.saveAll(nutrientEntities);
        portionRepository.save(new FoodPortion(
            food,
            request.defaultPortionLabel().trim(),
            request.defaultPortionGrams().setScale(2, RoundingMode.HALF_UP),
            true,
            1
        ));

        recordReview(
            food,
            reviewer,
            "CURATION_SAVED",
            fromStatus,
            food.getCurationStatus(),
            request.note()
        );

        auditService.record(
            reviewer,
            "FOOD_CURATION_UPDATED",
            "SUCCESS",
            "food:" + food.getId(),
            "sourceCode=" + source.getSourceCode()
                + ";nutrientCount=" + nutrientEntities.size()
        );

        nutrientRepository.flush();
        portionRepository.flush();

        return detail(food);
    }

    @Transactional
    public AdminFoodDetailResponse markReady(
        AarogyaPrincipal principal,
        UUID foodId,
        String note
    ) {
        var food = requireFood(foodId);

        if ("PUBLISHED".equals(food.getCurationStatus())) {
            throw new IllegalStateException(
                "Published food is already live."
            );
        }

        requirePublishReady(food);
        var reviewer = requireReviewer(principal.id());
        var fromStatus = food.getCurationStatus();

        food.markReadyToPublish();

        recordReview(
            food,
            reviewer,
            "MARKED_READY",
            fromStatus,
            food.getCurationStatus(),
            note
        );

        auditService.record(
            reviewer,
            "FOOD_MARKED_READY",
            "SUCCESS",
            "food:" + food.getId(),
            "curationStatus=" + food.getCurationStatus()
        );

        return detail(food);
    }

    @Transactional
    public AdminFoodDetailResponse publish(
        AarogyaPrincipal principal,
        UUID foodId,
        String note
    ) {
        var food = requireFood(foodId);

        if (!Set.of("READY_TO_PUBLISH", "UNPUBLISHED")
            .contains(food.getCurationStatus())) {
            throw new IllegalStateException(
                "Food must be ready to publish before it can go live."
            );
        }

        requirePublishReady(food);
        var reviewer = requireReviewer(principal.id());
        var fromStatus = food.getCurationStatus();

        food.publish();

        recordReview(
            food,
            reviewer,
            "PUBLISHED",
            fromStatus,
            food.getCurationStatus(),
            note
        );

        auditService.record(
            reviewer,
            "FOOD_PUBLISHED",
            "SUCCESS",
            "food:" + food.getId(),
            "slug=" + food.getSlug()
        );

        return detail(food);
    }

    @Transactional
    public AdminFoodDetailResponse unpublish(
        AarogyaPrincipal principal,
        UUID foodId,
        String note
    ) {
        var food = requireFood(foodId);

        if (!"PUBLISHED".equals(food.getCurationStatus())) {
            throw new IllegalStateException(
                "Only published food can be unpublished."
            );
        }

        var reviewer = requireReviewer(principal.id());
        var fromStatus = food.getCurationStatus();

        food.unpublish();

        recordReview(
            food,
            reviewer,
            "UNPUBLISHED",
            fromStatus,
            food.getCurationStatus(),
            note
        );

        auditService.record(
            reviewer,
            "FOOD_UNPUBLISHED",
            "SUCCESS",
            "food:" + food.getId(),
            "slug=" + food.getSlug()
        );

        return detail(food);
    }

    @Transactional
    public AdminFoodDetailResponse returnForChanges(
        AarogyaPrincipal principal,
        UUID foodId,
        String note
    ) {
        var food = requireFood(foodId);
        var normalizedNote = normalizeOptional(note);

        if (normalizedNote == null) {
            throw new IllegalArgumentException(
                "A review note is required when returning content for changes."
            );
        }

        if ("PUBLISHED".equals(food.getCurationStatus())) {
            throw new IllegalStateException(
                "Unpublish live content before returning it for changes."
            );
        }

        var reviewer = requireReviewer(principal.id());
        var fromStatus = food.getCurationStatus();

        food.returnForChanges();

        recordReview(
            food,
            reviewer,
            "RETURNED_FOR_CHANGES",
            fromStatus,
            food.getCurationStatus(),
            normalizedNote
        );

        auditService.record(
            reviewer,
            "FOOD_RETURNED_FOR_CHANGES",
            "SUCCESS",
            "food:" + food.getId(),
            "curationStatus=" + food.getCurationStatus()
        );

        return detail(food);
    }

    @Transactional(readOnly = true)
    public List<AdminSourceResponse> sources() {
        return sourceRepository.findAllByOrderByNameAsc().stream()
            .map(AdminSourceResponse::from)
            .toList();
    }

    @Transactional
    public AdminSourceResponse createSource(
        AarogyaPrincipal principal,
        CreateNutritionSourceRequest request
    ) {
        var code = normalizeCode(request.sourceCode());
        var type = normalizeCode(request.sourceType());

        if (!SOURCE_TYPES.contains(type)) {
            throw new IllegalArgumentException(
                "Unsupported nutrition source type."
            );
        }

        if (sourceRepository.existsBySourceCodeIgnoreCase(code)) {
            throw new IllegalArgumentException(
                "A nutrition source with this code already exists."
            );
        }

        var source = sourceRepository.save(new NutritionSource(
            code,
            request.name().trim(),
            normalizeOptional(request.versionLabel()),
            type,
            normalizeOptional(request.sourceUrl()),
            normalizeOptional(request.licenseLabel()),
            normalizeOptional(request.usageNote()),
            request.retrievedOn()
        ));

        var actor = requireReviewer(principal.id());

        auditService.record(
            actor,
            "NUTRITION_SOURCE_CREATED",
            "SUCCESS",
            "nutrition-source:" + source.getId(),
            "sourceCode=" + source.getSourceCode()
                + ";sourceType=" + source.getSourceType()
        );

        return AdminSourceResponse.from(source);
    }

    @Transactional(readOnly = true)
    public List<AuditEventResponse> auditEvents(
        String eventType,
        int limit
    ) {
        var safeLimit = Math.min(Math.max(limit, 1), 100);
        var page = PageRequest.of(
            0,
            safeLimit,
            Sort.by(Sort.Direction.DESC, "occurredAt")
        );
        var normalizedType = normalizeCode(eventType);

        var events = normalizedType == null
            ? auditRepository.findAll(page).getContent()
            : auditRepository.findByEventTypeOrderByOccurredAtDesc(
                normalizedType,
                page
            );

        return events.stream()
            .map(AuditEventResponse::from)
            .toList();
    }

    private AdminFoodDetailResponse detail(Food food) {
        var nutrients = nutrientRepository
            .findByFood_IdOrderByNutrientCodeAsc(food.getId());
        var portions = portionRepository
            .findByFood_IdOrderByDisplayOrderAsc(food.getId());
        var reviews = reviewRepository
            .findByFood_IdOrderByOccurredAtDesc(food.getId());

        return new AdminFoodDetailResponse(
            AdminFoodSummaryResponse.from(
                food,
                nutrients.size(),
                portions.size(),
                isPublishReady(food, nutrients, portions)
            ),
            food.getDescription(),
            food.getFoodType(),
            food.getCategoryCode(),
            food.getDietaryClassification(),
            food.getAllergens(),
            food.getAliases(),
            food.getSource() == null
                ? null
                : AdminSourceResponse.from(food.getSource()),
            food.getSourceFoodRef(),
            nutrients.stream().map(AdminNutrientResponse::from).toList(),
            portions.stream().map(AdminPortionResponse::from).toList(),
            reviews.stream().map(AdminReviewResponse::from).toList()
        );
    }

    private AdminFoodSummaryResponse summary(Food food) {
        var nutrients = nutrientRepository
            .findByFood_IdOrderByNutrientCodeAsc(food.getId());
        var portions = portionRepository
            .findByFood_IdOrderByDisplayOrderAsc(food.getId());

        return AdminFoodSummaryResponse.from(
            food,
            nutrients.size(),
            portions.size(),
            isPublishReady(food, nutrients, portions)
        );
    }

    private Map<String, CuratedNutrientRequest> validateNutrients(
        List<CuratedNutrientRequest> requests
    ) {
        var normalized = new LinkedHashMap<String, CuratedNutrientRequest>();

        for (var request : requests) {
            var code = normalizeCode(request.code());
            var unit = request.unit().trim();

            if (normalized.containsKey(code)) {
                throw new IllegalArgumentException(
                    "Duplicate nutrient code: " + code
                );
            }

            normalized.put(
                code,
                new CuratedNutrientRequest(
                    code,
                    request.amountPer100g(),
                    unit
                )
            );
        }

        for (var required : REQUIRED_NUTRIENTS.entrySet()) {
            var item = normalized.get(required.getKey());

            if (item == null) {
                throw new IllegalArgumentException(
                    "Missing required nutrient: " + required.getKey()
                );
            }

            if (!required.getValue().equalsIgnoreCase(item.unit())) {
                throw new IllegalArgumentException(
                    required.getKey()
                        + " must use unit "
                        + required.getValue()
                        + "."
                );
            }
        }

        return normalized;
    }

    private void requirePublishReady(Food food) {
        var nutrients = nutrientRepository
            .findByFood_IdOrderByNutrientCodeAsc(food.getId());
        var portions = portionRepository
            .findByFood_IdOrderByDisplayOrderAsc(food.getId());

        if (!isPublishReady(food, nutrients, portions)) {
            throw new IllegalStateException(
                "Food is missing publish-ready provenance, nutrients, or portion data."
            );
        }
    }

    private boolean isPublishReady(
        Food food,
        List<FoodNutrient> nutrients,
        List<FoodPortion> portions
    ) {
        if (!"SOURCE_REFERENCED".equals(food.getNutrientStatus())) {
            return false;
        }

        if (food.getSource() == null
            || !"FOOD_COMPOSITION".equals(food.getSource().getSourceType())
            || food.getSourceFoodRef() == null
            || food.getSourceFoodRef().isBlank()) {
            return false;
        }

        if (portions.stream().noneMatch(FoodPortion::isDefaultPortion)) {
            return false;
        }

        var byCode = new LinkedHashMap<String, FoodNutrient>();

        for (var nutrient : nutrients) {
            byCode.put(nutrient.getNutrientCode(), nutrient);
        }

        for (var required : REQUIRED_NUTRIENTS.entrySet()) {
            var nutrient = byCode.get(required.getKey());

            if (nutrient == null
                || !required.getValue().equalsIgnoreCase(nutrient.getUnit())) {
                return false;
            }
        }

        return true;
    }

    private Food requireFood(UUID foodId) {
        return foodRepository.findById(foodId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Food record was not found."
            ));
    }

    private UserAccount requireReviewer(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Staff account was not found."
            ));
    }

    private void recordReview(
        Food food,
        UserAccount reviewer,
        String action,
        String fromStatus,
        String toStatus,
        String note
    ) {
        reviewRepository.save(new FoodCurationReview(
            food,
            reviewer,
            reviewer.getRole().name(),
            action,
            fromStatus,
            toStatus,
            normalizeOptional(note)
        ));
    }

    private String normalizeStatus(String value) {
        var normalized = normalizeCode(value);

        if (normalized == null) {
            return null;
        }

        if (!CURATION_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException(
                "Unsupported curation status."
            );
        }

        return normalized;
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim()
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9_]+", "_");
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}

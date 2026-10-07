package in.aarogya.research.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.meals.domain.MealEntry;
import in.aarogya.meals.repository.MealEntryRepository;
import in.aarogya.profile.domain.ConsentRecord;
import in.aarogya.profile.repository.ConsentRecordRepository;
import in.aarogya.profile.repository.HealthProfileRepository;
import in.aarogya.research.api.ResearchCohortResponse;
import in.aarogya.research.api.ResearchConsentResponse;
import in.aarogya.research.api.ResearchConsentUpdateRequest;
import in.aarogya.research.api.ResearchEventResponse;
import in.aarogya.research.api.ResearchExposureResponse;
import in.aarogya.research.api.ResearchMetricDefinitionResponse;
import in.aarogya.research.api.ResearchMetricResponse;
import in.aarogya.research.api.ResearchOverviewResponse;
import in.aarogya.research.domain.ResearchFeatureEvent;
import in.aarogya.research.repository.ResearchFeatureEventRepository;
import in.aarogya.research.repository.ResearchMetricDefinitionRepository;
import in.aarogya.security.AarogyaPrincipal;
import in.aarogya.security.SecurityAuditService;

@Service
public class ResearchEvaluationService {

    public static final String RESEARCH_CONSENT = "RESEARCH_PARTICIPATION";
    public static final String RESEARCH_POLICY_VERSION = "2026-10-research-v1";
    public static final int MINIMUM_COHORT_SIZE = 5;

    private static final Set<String> EVENT_CODES = Set.of(
        "GUIDANCE_VIEWED",
        "PLANS_VIEWED",
        "PLAN_GENERATED",
        "ALERTS_VIEWED",
        "ANALYTICS_VIEWED",
        "REGIONAL_VIEWED"
    );

    private final ConsentRecordRepository consentRepository;
    private final ResearchFeatureEventRepository eventRepository;
    private final ResearchMetricDefinitionRepository definitionRepository;
    private final MealEntryRepository mealRepository;
    private final HealthProfileRepository profileRepository;
    private final UserAccountRepository userRepository;
    private final SecurityAuditService auditService;

    public ResearchEvaluationService(
        ConsentRecordRepository consentRepository,
        ResearchFeatureEventRepository eventRepository,
        ResearchMetricDefinitionRepository definitionRepository,
        MealEntryRepository mealRepository,
        HealthProfileRepository profileRepository,
        UserAccountRepository userRepository,
        SecurityAuditService auditService
    ) {
        this.consentRepository = consentRepository;
        this.eventRepository = eventRepository;
        this.definitionRepository = definitionRepository;
        this.mealRepository = mealRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public ResearchConsentResponse consent(UUID userId) {
        return consentRepository
            .findTopByUser_IdAndConsentTypeOrderByRecordedAtDesc(
                userId,
                RESEARCH_CONSENT
            )
            .map(record -> new ResearchConsentResponse(
                record.isGranted(),
                record.getPolicyVersion(),
                record.getRecordedAt(),
                record.isGranted()
            ))
            .orElse(new ResearchConsentResponse(
                false,
                null,
                null,
                false
            ));
    }

    @Transactional
    public ResearchConsentResponse updateConsent(
        UUID userId,
        ResearchConsentUpdateRequest request
    ) {
        if (!RESEARCH_POLICY_VERSION.equals(
            request.policyVersion().trim()
        )) {
            throw new IllegalArgumentException(
                "Unsupported research consent policy version."
            );
        }

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Account not found."
            ));

        var record = consentRepository.save(new ConsentRecord(
            user,
            RESEARCH_CONSENT,
            request.granted(),
            RESEARCH_POLICY_VERSION
        ));

        if (!request.granted()) {
            eventRepository.deleteByUser_Id(userId);
        }

        auditService.record(
            user,
            "RESEARCH_CONSENT_CHANGED",
            "SUCCESS",
            RESEARCH_CONSENT,
            request.granted()
                ? "{\"granted\":true}"
                : "{\"granted\":false,\"researchEventsDeleted\":true}"
        );

        return new ResearchConsentResponse(
            record.isGranted(),
            record.getPolicyVersion(),
            record.getRecordedAt(),
            record.isGranted()
        );
    }

    @Transactional
    public ResearchEventResponse recordEvent(
        UUID userId,
        String rawEventCode
    ) {
        var eventCode = normalizeEventCode(rawEventCode);

        if (!EVENT_CODES.contains(eventCode)) {
            throw new IllegalArgumentException(
                "Unsupported research feature event."
            );
        }

        var consent = consentRepository
            .findTopByUser_IdAndConsentTypeOrderByRecordedAtDesc(
                userId,
                RESEARCH_CONSENT
            );

        if (consent.isEmpty() || !consent.get().isGranted()) {
            return new ResearchEventResponse(
                false,
                "Research participation is not enabled."
            );
        }

        var eventDate = LocalDate.now(ZoneOffset.UTC);

        if (eventRepository
            .existsByUser_IdAndEventCodeAndEventDate(
                userId,
                eventCode,
                eventDate
            )) {
            return new ResearchEventResponse(
                false,
                "This daily feature exposure is already recorded."
            );
        }

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Account not found."
            ));

        eventRepository.save(new ResearchFeatureEvent(
            user,
            eventCode,
            1,
            eventDate
        ));

        return new ResearchEventResponse(
            true,
            "Daily feature exposure recorded."
        );
    }

    @Transactional(readOnly = true)
    public List<ResearchMetricDefinitionResponse> definitions() {
        return definitionRepository
            .findAllByOrderByMetricCodeAscMetricVersionDesc()
            .stream()
            .map(ResearchMetricDefinitionResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ResearchOverviewResponse overview(
        LocalDate requestedFrom,
        LocalDate requestedTo
    ) {
        var range = validateRange(requestedFrom, requestedTo);
        var participants = activeParticipants();
        var participantIds = participants.keySet();

        if (participantIds.isEmpty()) {
            return emptyOverview(range);
        }

        var earliestConsentDate = participants.values().stream()
            .map(instant -> instant.atZone(ZoneOffset.UTC).toLocalDate())
            .min(LocalDate::compareTo)
            .orElse(range.from());

        var allEvents = eventRepository
            .findByUser_IdInAndEventDateBetweenOrderByEventDateAsc(
                participantIds,
                earliestConsentDate,
                range.to()
            );

        var events = allEvents.stream()
            .filter(event -> !event.getEventDate().isBefore(range.from()))
            .toList();

        var extendedFrom = range.from().minusDays(7);
        var meals = mealRepository
            .findByUser_IdInAndMealDateBetweenOrderByMealDateAscCreatedAtAsc(
                participantIds,
                extendedFrom,
                range.to()
            )
            .stream()
            .filter(entry -> eligibleAfterConsent(
                entry,
                participants
            ))
            .toList();

        var currentMeals = meals.stream()
            .filter(entry ->
                !entry.getMealDate().isBefore(range.from())
                && !entry.getMealDate().isAfter(range.to())
            )
            .toList();

        var loggingDaysByUser = distinctMealDays(currentMeals);
        var activeLoggers = loggingDaysByUser.size();

        var averageLoggingDays = activeLoggers == 0
            ? null
            : decimal(
                loggingDaysByUser.values().stream()
                    .mapToInt(Set::size)
                    .average()
                    .orElse(0)
            );

        var prePost = prePostChange(
            meals,
            allEvents,
            participants,
            range
        );

        var metrics = List.of(
            metric(
                "OPTED_IN_PARTICIPANTS",
                "Opted-in participants",
                decimal(participants.size()),
                "participants",
                participants.size(),
                "Counts accounts whose latest research consent is granted."
            ),
            metric(
                "ACTIVE_LOGGERS",
                "Active meal loggers",
                decimal(activeLoggers),
                "participants",
                activeLoggers,
                "Participants with at least one eligible meal-log day in the selected window."
            ),
            metric(
                "LOGGING_DAYS_PER_ACTIVE_PARTICIPANT",
                "Logging days / active participant",
                averageLoggingDays,
                "days",
                activeLoggers,
                "Mean distinct meal-log days among active opted-in participants."
            ),
            metric(
                "PRE_POST_LOGGING_DAY_CHANGE_7D",
                "Associated 7-day logging change",
                prePost.averageChange(),
                "days",
                prePost.eligibleParticipants(),
                "Mean post-minus-pre logging-day difference around first recorded feature exposure. Observational association only."
            )
        );

        var exposures = exposureSummary(events);
        var cohorts = dietaryPatternCohorts(participantIds);

        return new ResearchOverviewResponse(
            range.from(),
            range.to(),
            MINIMUM_COHORT_SIZE,
            participants.size(),
            metrics,
            exposures,
            cohorts,
            List.of(
                "Only users whose latest RESEARCH_PARTICIPATION consent is granted are included.",
                "Meal activity before the latest consent grant is excluded.",
                "Feature events are de-duplicated to one event per feature per participant per UTC day.",
                "Cohorts and outcome metrics with fewer than five eligible participants are suppressed.",
                "Pre/post results describe association and must not be interpreted as causal impact.",
                "No names, emails, user IDs, raw health records or individual meal logs are returned by research endpoints."
            )
        );
    }

    @Transactional
    public String exportCsv(
        AarogyaPrincipal principal,
        LocalDate from,
        LocalDate to
    ) {
        var overview = overview(from, to);
        var csv = new StringBuilder();

        csv.append(
            "row_type,code_or_segment,version,window_from,window_to,"
        );
        csv.append(
            "eligible_participants,value,unit,interpretation\n"
        );

        var definitions = definitions().stream()
            .collect(Collectors.toMap(
                ResearchMetricDefinitionResponse::metricCode,
                item -> item,
                (left, right) ->
                    left.metricVersion() >= right.metricVersion()
                        ? left
                        : right
            ));

        for (var metric : overview.metrics()) {
            if (metric.suppressed()) {
                continue;
            }

            var definition = definitions.get(metric.metricCode());
            csv.append("metric,")
                .append(csv(metric.metricCode())).append(",")
                .append(definition == null ? "" : definition.metricVersion())
                .append(",")
                .append(overview.from()).append(",")
                .append(overview.to()).append(",")
                .append(metric.eligibleParticipants()).append(",")
                .append(metric.value()).append(",")
                .append(csv(metric.unit())).append(",")
                .append(csv(metric.interpretation()))
                .append("\n");
        }

        for (var exposure : overview.featureExposures()) {
            if (exposure.suppressed()) {
                continue;
            }

            csv.append("feature_exposure,")
                .append(csv(exposure.eventCode())).append(",1,")
                .append(overview.from()).append(",")
                .append(overview.to()).append(",")
                .append(exposure.participantCount()).append(",")
                .append(exposure.participantCount())
                .append(",participants,")
                .append(csv(
                    "Distinct opted-in participants with at least one de-duplicated daily feature exposure."
                ))
                .append("\n");
        }

        for (var cohort : overview.dietaryPatternCohorts()) {
            if (cohort.suppressed()) {
                continue;
            }

            csv.append("cohort,")
                .append(csv(
                    cohort.segmentType()
                        + ":"
                        + cohort.segmentValue()
                ))
                .append(",1,")
                .append(overview.from()).append(",")
                .append(overview.to()).append(",")
                .append(cohort.participantCount()).append(",")
                .append(cohort.participantCount())
                .append(",participants,")
                .append(csv(
                    "Current broad dietary-pattern cohort among opted-in participants."
                ))
                .append("\n");
        }

        var actor = userRepository.findById(principal.id())
            .orElseThrow(() -> new IllegalArgumentException(
                "Admin account not found."
            ));

        auditService.record(
            actor,
            "RESEARCH_AGGREGATE_EXPORT",
            "SUCCESS",
            "research-evaluation",
            "from=" + overview.from()
                + ";to=" + overview.to()
                + ";minCohort=" + MINIMUM_COHORT_SIZE
        );

        return csv.toString();
    }

    private ResearchOverviewResponse emptyOverview(
        EvaluationRange range
    ) {
        var metrics = List.of(
            metric(
                "OPTED_IN_PARTICIPANTS",
                "Opted-in participants",
                BigDecimal.ZERO,
                "participants",
                0,
                "No opted-in research participants are currently eligible."
            ),
            metric(
                "ACTIVE_LOGGERS",
                "Active meal loggers",
                BigDecimal.ZERO,
                "participants",
                0,
                "No opted-in research participants are currently eligible."
            ),
            metric(
                "LOGGING_DAYS_PER_ACTIVE_PARTICIPANT",
                "Logging days / active participant",
                null,
                "days",
                0,
                "No eligible cohort is available."
            ),
            metric(
                "PRE_POST_LOGGING_DAY_CHANGE_7D",
                "Associated 7-day logging change",
                null,
                "days",
                0,
                "No eligible exposure cohort is available."
            )
        );

        return new ResearchOverviewResponse(
            range.from(),
            range.to(),
            MINIMUM_COHORT_SIZE,
            0,
            metrics,
            List.of(),
            List.of(),
            List.of(
                "Research metrics remain suppressed until at least five eligible participants contribute to a cohort.",
                "No identifiable research dataset is exposed."
            )
        );
    }

    private Map<UUID, Instant> activeParticipants() {
        var latest = new LinkedHashMap<UUID, ConsentRecord>();

        for (var record : consentRepository
            .findByConsentTypeOrderByRecordedAtAsc(
                RESEARCH_CONSENT
            )) {
            latest.put(record.getUser().getId(), record);
        }

        var active = new LinkedHashMap<UUID, Instant>();

        for (var entry : latest.entrySet()) {
            if (entry.getValue().isGranted()) {
                active.put(
                    entry.getKey(),
                    entry.getValue().getRecordedAt()
                );
            }
        }

        return active;
    }

    private boolean eligibleAfterConsent(
        MealEntry entry,
        Map<UUID, Instant> participants
    ) {
        var grantedAt = participants.get(
            entry.getUser().getId()
        );

        return grantedAt != null
            && !entry.getCreatedAt().isBefore(grantedAt);
    }

    private Map<UUID, Set<LocalDate>> distinctMealDays(
        List<MealEntry> meals
    ) {
        var result = new LinkedHashMap<UUID, Set<LocalDate>>();

        for (var entry : meals) {
            result.computeIfAbsent(
                entry.getUser().getId(),
                ignored -> new LinkedHashSet<>()
            ).add(entry.getMealDate());
        }

        return result;
    }

    private PrePostResult prePostChange(
        List<MealEntry> meals,
        List<ResearchFeatureEvent> events,
        Map<UUID, Instant> participants,
        EvaluationRange range
    ) {
        var firstExposure = new LinkedHashMap<UUID, LocalDate>();

        for (var event : events) {
            firstExposure.putIfAbsent(
                event.getUser().getId(),
                event.getEventDate()
            );
        }

        var mealDays = distinctMealDays(meals);
        var changes = new ArrayList<Integer>();
        var latestEligibleExposure = range.to().minusDays(7);

        for (var exposure : firstExposure.entrySet()) {
            var userId = exposure.getKey();
            var exposureDate = exposure.getValue();
            var consentAt = participants.get(userId);

            if (exposureDate.isBefore(range.from())
                || exposureDate.isAfter(latestEligibleExposure)) {
                continue;
            }

            var preFrom = exposureDate.minusDays(7);
            var preTo = exposureDate.minusDays(1);
            var postFrom = exposureDate.plusDays(1);
            var postTo = exposureDate.plusDays(7);

            if (consentAt == null
                || consentAt.atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .isAfter(preFrom)) {
                continue;
            }

            var dates = mealDays.getOrDefault(
                userId,
                Set.of()
            );

            var pre = countBetween(
                dates,
                preFrom,
                preTo
            );
            var post = countBetween(
                dates,
                postFrom,
                postTo
            );

            changes.add(post - pre);
        }

        if (changes.isEmpty()) {
            return new PrePostResult(0, null);
        }

        return new PrePostResult(
            changes.size(),
            decimal(
                changes.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0)
            )
        );
    }

    private int countBetween(
        Set<LocalDate> dates,
        LocalDate from,
        LocalDate to
    ) {
        return (int) dates.stream()
            .filter(date ->
                !date.isBefore(from)
                && !date.isAfter(to)
            )
            .count();
    }

    private List<ResearchExposureResponse> exposureSummary(
        List<ResearchFeatureEvent> events
    ) {
        var participantsByCode =
            new LinkedHashMap<String, Set<UUID>>();

        for (var event : events) {
            participantsByCode
                .computeIfAbsent(
                    event.getEventCode(),
                    ignored -> new LinkedHashSet<>()
                )
                .add(event.getUser().getId());
        }

        return EVENT_CODES.stream()
            .sorted()
            .map(code -> {
                var count = participantsByCode
                    .getOrDefault(code, Set.of())
                    .size();
                var suppressed =
                    count < MINIMUM_COHORT_SIZE;

                return new ResearchExposureResponse(
                    code,
                    suppressed ? null : count,
                    suppressed
                );
            })
            .toList();
    }

    private List<ResearchCohortResponse> dietaryPatternCohorts(
        Set<UUID> participantIds
    ) {
        if (participantIds.isEmpty()) {
            return List.of();
        }

        var counts = new LinkedHashMap<String, Integer>();

        for (var profile : profileRepository
            .findByUserIdIn(participantIds)) {
            var value = profile.getDietaryPattern() == null
                ? "NOT_PROVIDED"
                : profile.getDietaryPattern();

            counts.merge(value, 1, Integer::sum);
        }

        return counts.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                var suppressed =
                    entry.getValue() < MINIMUM_COHORT_SIZE;

                return new ResearchCohortResponse(
                    "DIETARY_PATTERN",
                    entry.getKey(),
                    suppressed ? null : entry.getValue(),
                    suppressed
                );
            })
            .toList();
    }

    private ResearchMetricResponse metric(
        String code,
        String label,
        BigDecimal value,
        String unit,
        int eligibleParticipants,
        String interpretation
    ) {
        var suppressed =
            eligibleParticipants < MINIMUM_COHORT_SIZE;

        return new ResearchMetricResponse(
            code,
            label,
            suppressed ? null : value,
            unit,
            eligibleParticipants,
            suppressed,
            interpretation
        );
    }

    private EvaluationRange validateRange(
        LocalDate requestedFrom,
        LocalDate requestedTo
    ) {
        var today = LocalDate.now(ZoneOffset.UTC);
        var to = requestedTo == null ? today : requestedTo;
        var from = requestedFrom == null
            ? to.minusDays(29)
            : requestedFrom;

        if (from.isAfter(to)) {
            throw new IllegalArgumentException(
                "Research window start must be on or before end."
            );
        }

        if (to.isAfter(today)) {
            throw new IllegalArgumentException(
                "Research evaluation cannot include future dates."
            );
        }

        if (ChronoUnit.DAYS.between(from, to) > 89) {
            throw new IllegalArgumentException(
                "Research evaluation windows are limited to 90 days."
            );
        }

        return new EvaluationRange(from, to);
    }

    private String normalizeEventCode(String value) {
        return value.trim()
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9_]+", "_");
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }

        return "\""
            + value.replace("\"", "\"\"")
            + "\"";
    }

    private record EvaluationRange(
        LocalDate from,
        LocalDate to
    ) {
    }

    private record PrePostResult(
        int eligibleParticipants,
        BigDecimal averageChange
    ) {
    }
}

package in.aarogya.nudges.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.health.repository.HealthObservationRepository;
import in.aarogya.health.service.HealthRecordService;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.nudges.api.NudgeEvaluationResponse;
import in.aarogya.nudges.api.NudgeResponse;
import in.aarogya.nudges.api.NudgeSummaryResponse;
import in.aarogya.nudges.domain.NudgeInstance;
import in.aarogya.nudges.domain.NudgeRule;
import in.aarogya.nudges.repository.NudgeInstanceRepository;
import in.aarogya.nudges.repository.NudgeRuleRepository;
import in.aarogya.profile.service.ProfileService;
import in.aarogya.recommendations.api.RecommendationItemResponse;
import in.aarogya.recommendations.service.RecommendationEngineService;
import in.aarogya.security.SecurityAuditService;

@Service
public class NudgeService {

    private static final BigDecimal PROFILE_WEIGHT_REVIEW_DELTA_KG =
        new BigDecimal("2.0");

    private static final List<String> CURRENT_STATUSES = List.of(
        "ACTIVE", "SNOOZED"
    );

    private final NudgeRuleRepository ruleRepository;
    private final NudgeInstanceRepository instanceRepository;
    private final RecommendationEngineService recommendationEngine;
    private final ProfileService profileService;
    private final HealthRecordService healthRecordService;
    private final HealthObservationRepository observationRepository;
    private final UserAccountRepository userRepository;
    private final SecurityAuditService auditService;

    public NudgeService(
        NudgeRuleRepository ruleRepository,
        NudgeInstanceRepository instanceRepository,
        RecommendationEngineService recommendationEngine,
        ProfileService profileService,
        HealthRecordService healthRecordService,
        HealthObservationRepository observationRepository,
        UserAccountRepository userRepository,
        SecurityAuditService auditService
    ) {
        this.ruleRepository = ruleRepository;
        this.instanceRepository = instanceRepository;
        this.recommendationEngine = recommendationEngine;
        this.profileService = profileService;
        this.healthRecordService = healthRecordService;
        this.observationRepository = observationRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public NudgeEvaluationResponse evaluate(
        UUID userId,
        LocalDate requestedDate
    ) {
        var date = requestedDate == null ? LocalDate.now() : requestedDate;
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        var rules = latestRules();
        var signals = new LinkedHashMap<String, NudgeSignal>();

        var assessment = recommendationEngine.assess(userId, date);

        for (var recommendation : assessment.recommendations()) {
            var signal = fromRecommendation(recommendation);
            if (signal != null) {
                signals.putIfAbsent(signal.nudgeKey(), signal);
            }
        }

        var profile = profileService.getProfile(userId);
        var healthConsent = healthRecordService.consent(userId);

        if (profile.personalizationConsentGranted()
            && healthConsent.granted()
            && profile.weightKg() != null) {
            manualWeightSignal(userId, profile.weightKg())
                .ifPresent(signal ->
                    signals.putIfAbsent(signal.nudgeKey(), signal)
                );
        }

        var now = Instant.now();
        var activeKeys = signals.keySet();

        for (var signal : signals.values()) {
            var rule = rules.get(signal.ruleCode());
            if (rule == null) {
                continue;
            }

            var existing = instanceRepository
                .findByUser_IdAndNudgeKey(userId, signal.nudgeKey())
                .orElse(null);

            if (existing == null) {
                instanceRepository.save(new NudgeInstance(
                    user,
                    signal.nudgeKey(),
                    rule,
                    signal.title(),
                    signal.message(),
                    signal.actionLabel(),
                    signal.actionHref(),
                    signal.reasonCode(),
                    signal.sourceType(),
                    signal.sourceRef(),
                    signal.evidenceLabel(),
                    signal.evidenceUrl()
                ));
            } else {
                existing.refresh(
                    rule,
                    signal.title(),
                    signal.message(),
                    signal.actionLabel(),
                    signal.actionHref(),
                    signal.reasonCode(),
                    signal.sourceType(),
                    signal.sourceRef(),
                    signal.evidenceLabel(),
                    signal.evidenceUrl(),
                    now
                );
            }
        }

        var resolved = 0;

        for (var existing : instanceRepository
            .findByUser_IdAndStatusInOrderByLastEvaluatedAtDesc(
                userId,
                CURRENT_STATUSES
            )) {
            if (!activeKeys.contains(existing.getNudgeKey())) {
                existing.resolve(now);
                resolved++;
            }
        }

        var activeAfter = instanceRepository
            .findByUser_IdAndStatusInOrderByLastEvaluatedAtDesc(
                userId,
                List.of("ACTIVE")
            )
            .size();

        auditService.record(
            user,
            "NUDGES_EVALUATED",
            "SUCCESS",
            "nudges",
            "signals=" + signals.size()
                + ";active=" + activeAfter
                + ";resolved=" + resolved
        );

        return new NudgeEvaluationResponse(
            date,
            signals.size(),
            activeAfter,
            resolved
        );
    }

    @Transactional(readOnly = true)
    public List<NudgeResponse> list(UUID userId, boolean includeHistory) {
        var items = includeHistory
            ? instanceRepository.findByUser_IdOrderByLastEvaluatedAtDesc(userId)
            : instanceRepository
                .findByUser_IdAndStatusInOrderByLastEvaluatedAtDesc(
                    userId,
                    CURRENT_STATUSES
                );

        return items.stream()
            .sorted(
                Comparator
                    .comparingInt(
                        (NudgeInstance item) -> severityRank(item.getSeverity())
                    )
                    .thenComparing(
                        NudgeInstance::getLastEvaluatedAt,
                        Comparator.reverseOrder()
                    )
            )
            .map(NudgeResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public NudgeSummaryResponse summary(UUID userId) {
        var current = instanceRepository
            .findByUser_IdAndStatusInOrderByLastEvaluatedAtDesc(
                userId,
                CURRENT_STATUSES
            );

        var active = current.stream()
            .filter(item -> "ACTIVE".equals(item.getStatus()))
            .count();
        var attention = current.stream()
            .filter(item -> "ACTIVE".equals(item.getStatus()))
            .filter(item -> "ATTENTION".equals(item.getSeverity()))
            .count();
        var snoozed = current.stream()
            .filter(item -> "SNOOZED".equals(item.getStatus()))
            .count();

        return new NudgeSummaryResponse(active, attention, snoozed);
    }

    @Transactional
    public NudgeResponse snooze(UUID userId, UUID nudgeId, int hours) {
        var nudge = requireCurrent(userId, nudgeId);
        nudge.snooze(Instant.now().plusSeconds(hours * 3600L));
        audit(userId, nudge, "NUDGE_SNOOZED", "hours=" + hours);
        return NudgeResponse.from(nudge);
    }

    @Transactional
    public NudgeResponse acknowledge(UUID userId, UUID nudgeId) {
        var nudge = requireCurrent(userId, nudgeId);
        nudge.acknowledge(Instant.now());
        audit(userId, nudge, "NUDGE_ACKNOWLEDGED", null);
        return NudgeResponse.from(nudge);
    }

    @Transactional
    public NudgeResponse dismiss(UUID userId, UUID nudgeId) {
        var nudge = requireCurrent(userId, nudgeId);
        nudge.dismiss(Instant.now());
        audit(userId, nudge, "NUDGE_DISMISSED", null);
        return NudgeResponse.from(nudge);
    }

    private NudgeInstance requireCurrent(UUID userId, UUID nudgeId) {
        var nudge = instanceRepository.findByIdAndUser_Id(nudgeId, userId)
            .orElseThrow(NudgeNotFoundException::new);

        if (!CURRENT_STATUSES.contains(nudge.getStatus())) {
            throw new IllegalArgumentException(
                "Only active or snoozed nudges can be changed."
            );
        }

        return nudge;
    }

    private void audit(
        UUID userId,
        NudgeInstance nudge,
        String eventType,
        String metadata
    ) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        auditService.record(
            user,
            eventType,
            "SUCCESS",
            nudge.getRuleCode(),
            metadata
        );
    }

    private Map<String, NudgeRule> latestRules() {
        var latest = new LinkedHashMap<String, NudgeRule>();

        for (var rule : ruleRepository
            .findByActiveTrueOrderByRuleCodeAscRuleVersionDesc()) {
            latest.putIfAbsent(rule.getRuleCode(), rule);
        }

        return latest;
    }

    private NudgeSignal fromRecommendation(
        RecommendationItemResponse recommendation
    ) {
        var nudgeRule = switch (recommendation.ruleCode()) {
            case "ALLERGEN_CONFLICT" -> "ALLERGEN_CONFLICT_NUDGE";
            case "DIETARY_PATTERN_CONFLICT" -> "DIETARY_PATTERN_REVIEW";
            case "FIBRE_TREND_LOW" -> "FIBRE_TREND_NUDGE";
            case "PROTEIN_TREND_LOW" -> "PROTEIN_TREND_NUDGE";
            default -> null;
        };

        if (nudgeRule == null) {
            return null;
        }

        var evidence = recommendation.evidence();

        return new NudgeSignal(
            "RULE:" + nudgeRule,
            nudgeRule,
            recommendation.title(),
            recommendation.observation() + " " + recommendation.consideration(),
            "Review explanation",
            "/guidance",
            recommendation.reasonCode(),
            "RECOMMENDATION",
            recommendation.ruleCode() + ":v" + recommendation.ruleVersion(),
            evidence == null ? null : evidence.name(),
            evidence == null ? null : evidence.url()
        );
    }

    private java.util.Optional<NudgeSignal> manualWeightSignal(
        UUID userId,
        BigDecimal profileWeight
    ) {
        return observationRepository
            .findFirstByHealthRecord_User_IdAndHealthRecord_SourceTypeAndObservationCodeAndValueNumericIsNotNullOrderByObservedAtDesc(
                userId,
                "MANUAL",
                "BODY_WEIGHT"
            )
            .filter(observation ->
                observation.getUnit() != null
                && "kg".equalsIgnoreCase(observation.getUnit().trim())
            )
            .filter(observation ->
                observation.getValueNumeric()
                    .subtract(profileWeight)
                    .abs()
                    .compareTo(PROFILE_WEIGHT_REVIEW_DELTA_KG) >= 0
            )
            .map(observation -> {
                var delta = observation.getValueNumeric()
                    .subtract(profileWeight)
                    .abs()
                    .setScale(1, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString();

                return new NudgeSignal(
                    "RULE:PROFILE_WEIGHT_REVIEW",
                    "PROFILE_WEIGHT_REVIEW",
                    "Review the weight stored in your profile",
                    "A self-reported weight measurement differs from the weight currently stored in your profile by "
                        + delta
                        + " kg. Review which value you want Aarogya to use for nutrition personalization. This is a data-consistency prompt, not an assessment of weight change.",
                    "Review profile",
                    "/profile",
                    "MANUAL_WEIGHT_DIFFERS_FROM_PROFILE",
                    "HEALTH_RECORD",
                    observation.getId().toString(),
                    "Aarogya product heuristic",
                    null
                );
            });
    }

    private int severityRank(String severity) {
        return switch (severity) {
            case "ATTENTION" -> 0;
            case "STANDARD" -> 1;
            default -> 2;
        };
    }

    private record NudgeSignal(
        String nudgeKey,
        String ruleCode,
        String title,
        String message,
        String actionLabel,
        String actionHref,
        String reasonCode,
        String sourceType,
        String sourceRef,
        String evidenceLabel,
        String evidenceUrl
    ) {
    }
}

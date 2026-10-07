package in.aarogya.profile.service;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.profile.api.ConsentUpdateRequest;
import in.aarogya.profile.api.ProfileResponse;
import in.aarogya.profile.api.ProfileUpdateRequest;
import in.aarogya.profile.domain.ConsentRecord;
import in.aarogya.profile.domain.HealthProfile;
import in.aarogya.profile.repository.ConsentRecordRepository;
import in.aarogya.profile.repository.HealthProfileRepository;
import in.aarogya.security.SecurityAuditService;

@Service
public class ProfileService {

    public static final String PERSONALIZATION_CONSENT = "HEALTH_PROFILE_PERSONALIZATION";
    public static final String CURRENT_POLICY_VERSION = "2026-10";

    private static final Set<String> SEX_OPTIONS = Set.of(
        "FEMALE", "MALE", "INTERSEX_OR_OTHER", "PREFER_NOT_TO_SAY"
    );

    private static final Set<String> ACTIVITY_OPTIONS = Set.of(
        "SEDENTARY", "LIGHT", "MODERATE", "HIGH", "VERY_HIGH"
    );

    private static final Set<String> DIET_OPTIONS = Set.of(
        "VEGETARIAN", "VEGAN", "EGGETARIAN", "NON_VEGETARIAN",
        "PESCATARIAN", "JAIN", "OTHER"
    );

    private static final Set<String> GOAL_OPTIONS = Set.of(
        "BALANCED_NUTRITION", "WEIGHT_MANAGEMENT", "MUSCLE_GAIN",
        "ENERGY", "HEART_HEALTH"
    );

    private static final Set<String> ALLERGY_OPTIONS = Set.of(
        "PEANUT", "TREE_NUT", "MILK", "EGG", "WHEAT",
        "SOY", "SESAME", "FISH", "SHELLFISH"
    );

    private static final Set<String> HEALTH_CONTEXT_OPTIONS = Set.of(
        "DIABETES", "HYPERTENSION", "ANEMIA", "HIGH_CHOLESTEROL",
        "PCOS", "THYROID_CONDITION", "KIDNEY_CONDITION",
        "PREGNANCY_OR_BREASTFEEDING", "OTHER"
    );

    private final HealthProfileRepository profileRepository;
    private final ConsentRecordRepository consentRepository;
    private final UserAccountRepository userRepository;
    private final SecurityAuditService auditService;

    public ProfileService(
        HealthProfileRepository profileRepository,
        ConsentRecordRepository consentRepository,
        UserAccountRepository userRepository,
        SecurityAuditService auditService
    ) {
        this.profileRepository = profileRepository;
        this.consentRepository = consentRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        var consent = latestConsent(userId);

        return profileRepository.findByUserId(userId)
            .map(profile -> ProfileResponse.from(
                profile,
                consent.map(ConsentRecord::isGranted).orElse(false),
                consent.map(ConsentRecord::getPolicyVersion).orElse(null),
                consent.map(ConsentRecord::getRecordedAt).orElse(null)
            ))
            .orElseGet(() -> ProfileResponse.empty(consent.orElse(null)));
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        requireGrantedConsent(userId);
        validateCodes(request);

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        var profile = profileRepository.findByUserId(userId)
            .orElseGet(() -> new HealthProfile(user));

        profile.update(
            request.ageYears(),
            normalizeCode(request.sexForNutrition()),
            request.heightCm(),
            request.weightKg(),
            normalizeCode(request.activityLevel()),
            normalizeCode(request.dietaryPattern()),
            request.stateOrRegion(),
            safeSet(request.goals()),
            safeSet(request.allergies()),
            safeSet(request.healthContexts())
        );

        profileRepository.save(profile);
        auditService.record(user, "PROFILE_UPDATED", "SUCCESS", user.getEmail(), null);
        return getProfile(userId);
    }

    @Transactional
    public ProfileResponse recordConsent(UUID userId, ConsentUpdateRequest request) {
        if (!CURRENT_POLICY_VERSION.equals(request.policyVersion().trim())) {
            throw new IllegalArgumentException("Unsupported consent policy version.");
        }

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        consentRepository.save(new ConsentRecord(
            user,
            PERSONALIZATION_CONSENT,
            request.granted(),
            request.policyVersion().trim()
        ));

        auditService.record(
            user,
            "CONSENT_CHANGED",
            "SUCCESS",
            PERSONALIZATION_CONSENT,
            request.granted() ? "{\"granted\":true}" : "{\"granted\":false}"
        );

        return getProfile(userId);
    }

    @Transactional
    public ProfileResponse completeOnboarding(UUID userId) {
        var profile = profileRepository.findByUserId(userId)
            .orElseThrow(() -> new ProfileIncompleteException(
                "Add your profile details before finishing setup."
            ));

        var consent = requireGrantedConsent(userId);

        if (profile.getAgeYears() == null
            || profile.getActivityLevel() == null
            || profile.getDietaryPattern() == null
            || profile.getGoals().isEmpty()) {
            throw new ProfileIncompleteException(
                "Age, activity level, dietary pattern and at least one goal are required."
            );
        }

        profile.markOnboardingComplete();

        var user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        auditService.record(user, "ONBOARDING_COMPLETED", "SUCCESS", user.getEmail(), null);

        return ProfileResponse.from(
            profile,
            true,
            consent.getPolicyVersion(),
            consent.getRecordedAt()
        );
    }

    private ConsentRecord requireGrantedConsent(UUID userId) {
        return latestConsent(userId)
            .filter(ConsentRecord::isGranted)
            .orElseThrow(() -> new ProfileIncompleteException(
                "Consent is required before health profile data can be stored or used."
            ));
    }

    private java.util.Optional<ConsentRecord> latestConsent(UUID userId) {
        return consentRepository
            .findTopByUser_IdAndConsentTypeOrderByRecordedAtDesc(
                userId,
                PERSONALIZATION_CONSENT
            );
    }

    private void validateCodes(ProfileUpdateRequest request) {
        validateOptional(request.sexForNutrition(), SEX_OPTIONS, "sex for nutrition");
        validateOptional(request.activityLevel(), ACTIVITY_OPTIONS, "activity level");
        validateOptional(request.dietaryPattern(), DIET_OPTIONS, "dietary pattern");
        validateSet(request.goals(), GOAL_OPTIONS, "goal");
        validateSet(request.allergies(), ALLERGY_OPTIONS, "allergy");
        validateSet(request.healthContexts(), HEALTH_CONTEXT_OPTIONS, "health context");
    }

    private void validateOptional(String value, Set<String> allowed, String label) {
        if (value == null || value.isBlank()) {
            return;
        }

        if (!allowed.contains(normalizeCode(value))) {
            throw new IllegalArgumentException("Unsupported " + label + " value.");
        }
    }

    private void validateSet(Set<String> values, Set<String> allowed, String label) {
        for (var value : safeSet(values)) {
            if (!allowed.contains(normalizeCode(value))) {
                throw new IllegalArgumentException("Unsupported " + label + " value.");
            }
        }
    }

    private Set<String> safeSet(Set<String> values) {
        if (values == null) {
            return Set.of();
        }

        return values.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(this::normalizeCode)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}

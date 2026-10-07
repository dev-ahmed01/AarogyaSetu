package in.aarogya.profile.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import in.aarogya.profile.domain.ConsentRecord;
import in.aarogya.profile.domain.HealthProfile;

public record ProfileResponse(
    Integer ageYears,
    String sexForNutrition,
    BigDecimal heightCm,
    BigDecimal weightKg,
    String activityLevel,
    String dietaryPattern,
    String stateOrRegion,
    Set<String> goals,
    Set<String> allergies,
    Set<String> healthContexts,
    boolean personalizationConsentGranted,
    String consentPolicyVersion,
    Instant consentRecordedAt,
    boolean onboardingComplete,
    Instant onboardingCompletedAt
) {

    public static ProfileResponse empty(ConsentRecord consent) {
        return new ProfileResponse(
            null, null, null, null, null, null, null,
            Set.of(), Set.of(), Set.of(),
            consent != null && consent.isGranted(),
            consent == null ? null : consent.getPolicyVersion(),
            consent == null ? null : consent.getRecordedAt(),
            false,
            null
        );
    }

    public static ProfileResponse from(
        HealthProfile profile,
        boolean consentGranted,
        String policyVersion,
        Instant consentRecordedAt
    ) {
        return new ProfileResponse(
            profile.getAgeYears(),
            profile.getSexForNutrition(),
            profile.getHeightCm(),
            profile.getWeightKg(),
            profile.getActivityLevel(),
            profile.getDietaryPattern(),
            profile.getStateOrRegion(),
            profile.getGoals(),
            profile.getAllergies(),
            profile.getHealthContexts(),
            consentGranted,
            policyVersion,
            consentRecordedAt,
            profile.getOnboardingCompletedAt() != null,
            profile.getOnboardingCompletedAt()
        );
    }
}

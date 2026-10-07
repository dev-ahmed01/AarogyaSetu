package in.aarogya.research.api;

import jakarta.validation.constraints.NotBlank;

public record ResearchConsentUpdateRequest(
    boolean granted,
    @NotBlank String policyVersion
) {
}

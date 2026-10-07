package in.aarogya.profile.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConsentUpdateRequest(
    boolean granted,

    @NotBlank
    @Size(max = 30)
    String policyVersion
) {
}

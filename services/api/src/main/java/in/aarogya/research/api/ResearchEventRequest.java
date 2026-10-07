package in.aarogya.research.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResearchEventRequest(
    @NotBlank
    @Size(max = 80)
    String eventCode
) {
}

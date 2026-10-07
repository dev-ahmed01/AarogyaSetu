package in.aarogya.health.api;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HealthRecordCreateRequest(
    @NotBlank @Size(max = 50) String recordType,
    @NotBlank @Size(max = 220) String title,
    @Size(max = 1600) String summaryText,
    @NotNull LocalDate clinicalDate,
    @Size(max = 220) String providerName,
    @Size(max = 220) String facilityName,
    @Valid @Size(max = 30) List<HealthObservationRequest> observations
) {
}

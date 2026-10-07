package in.aarogya.administration.api;

import jakarta.validation.constraints.Size;

public record ReviewNoteRequest(
    @Size(max = 1000)
    String note
) {
}

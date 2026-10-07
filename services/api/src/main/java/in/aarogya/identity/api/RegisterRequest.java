package in.aarogya.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank
    @Email
    @Size(max = 320)
    String email,

    @NotBlank
    @Size(min = 2, max = 120)
    String displayName,

    @NotBlank
    @Size(min = 10, max = 72)
    String password
) {
}

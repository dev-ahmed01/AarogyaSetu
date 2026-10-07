package in.aarogya.identity.api;

import java.util.UUID;

import in.aarogya.identity.domain.UserAccount;

public record UserResponse(
    UUID id,
    String email,
    String displayName,
    String role
) {

    public static UserResponse from(UserAccount account) {
        return new UserResponse(
            account.getId(),
            account.getEmail(),
            account.getDisplayName(),
            account.getRole().name()
        );
    }
}

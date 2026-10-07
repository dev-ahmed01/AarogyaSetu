package in.aarogya.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import in.aarogya.identity.domain.UserAccount;

public record AarogyaPrincipal(
    UUID id,
    String email,
    String displayName,
    String passwordHash,
    String role,
    boolean enabled
) implements UserDetails {

    public static AarogyaPrincipal from(UserAccount account) {
        return new AarogyaPrincipal(
            account.getId(),
            account.getEmail(),
            account.getDisplayName(),
            account.getPasswordHash(),
            account.getRole().name(),
            account.isEnabled()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

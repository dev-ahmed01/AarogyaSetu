package in.aarogya.security;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import in.aarogya.identity.repository.UserAccountRepository;

@Service
public class AarogyaUserDetailsService implements UserDetailsService {

    private final UserAccountRepository repository;

    public AarogyaUserDetailsService(UserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmailIgnoreCase(email)
            .map(AarogyaPrincipal::from)
            .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }

    public AarogyaPrincipal loadById(UUID id) throws UsernameNotFoundException {
        return repository.findById(id)
            .map(AarogyaPrincipal::from)
            .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    }
}

package in.aarogya.identity.service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.identity.api.LoginRequest;
import in.aarogya.identity.api.RegisterRequest;
import in.aarogya.identity.api.UserResponse;
import in.aarogya.identity.domain.RefreshSession;
import in.aarogya.identity.domain.UserAccount;
import in.aarogya.identity.domain.UserRole;
import in.aarogya.identity.repository.RefreshSessionRepository;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.security.JwtService;
import in.aarogya.security.SecurityAuditService;
import in.aarogya.security.TokenHashingService;
import io.jsonwebtoken.JwtException;

@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenHashingService tokenHashingService;
    private final SecurityAuditService auditService;

    public AuthService(
        UserAccountRepository userRepository,
        RefreshSessionRepository refreshSessionRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        TokenHashingService tokenHashingService,
        SecurityAuditService auditService
    ) {
        this.userRepository = userRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenHashingService = tokenHashingService;
        this.auditService = auditService;
    }

    @Transactional
    public SessionBundle register(RegisterRequest request) {
        var email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AccountExistsException();
        }

        var user = userRepository.save(new UserAccount(
            email,
            passwordEncoder.encode(request.password()),
            request.displayName().trim(),
            UserRole.USER
        ));

        var session = issueSession(user);

        auditService.record(
            user,
            "ACCOUNT_REGISTERED",
            "SUCCESS",
            email,
            null
        );

        return session;
    }

    public SessionBundle login(LoginRequest request) {
        var email = normalizeEmail(request.email());

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (BadCredentialsException exception) {
            auditService.record(
                null,
                "LOGIN",
                "FAILURE",
                email,
                "{"reason":"bad_credentials"}"
            );
            throw exception;
        }

        var user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        var session = issueSession(user);

        auditService.record(
            user,
            "LOGIN",
            "SUCCESS",
            email,
            null
        );

        return session;
    }

    @Transactional
    public SessionBundle refresh(String refreshToken) {
        try {
            var claims = jwtService.parseRefreshToken(refreshToken);
            var tokenHash = tokenHashingService.sha256(refreshToken);
            var storedSession = refreshSessionRepository
                .findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

            var user = storedSession.getUser();
            var tokenUserId = jwtService.subjectAsUserId(claims);

            if (!storedSession.isActiveAt(Instant.now())
                || !user.getId().equals(tokenUserId)
                || !user.isEnabled()) {
                throw new InvalidRefreshTokenException();
            }

            storedSession.markUsedAndRevoke();
            var session = issueSession(user);

            auditService.record(
                user,
                "TOKEN_REFRESH",
                "SUCCESS",
                user.getEmail(),
                null
            );

            return session;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidRefreshTokenException();
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        var tokenHash = tokenHashingService.sha256(refreshToken);

        refreshSessionRepository
            .findByTokenHashAndRevokedAtIsNull(tokenHash)
            .ifPresent(session -> {
                session.revoke();
                auditService.record(
                    session.getUser(),
                    "LOGOUT",
                    "SUCCESS",
                    session.getUser().getEmail(),
                    null
                );
            });
    }

    @Transactional(readOnly = true)
    public UserAccount requireUser(java.util.UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException("Account not found"));
    }

    private SessionBundle issueSession(UserAccount user) {
        var accessToken = jwtService.issueAccessToken(user);
        var refreshToken = jwtService.issueRefreshToken(user);

        refreshSessionRepository.save(new RefreshSession(
            user,
            tokenHashingService.sha256(refreshToken.value()),
            refreshToken.expiresAt()
        ));

        return new SessionBundle(
            UserResponse.from(user),
            accessToken.value(),
            accessToken.expiresAt(),
            refreshToken.value(),
            refreshToken.expiresAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public record SessionBundle(
        UserResponse user,
        String accessToken,
        Instant accessExpiresAt,
        String refreshToken,
        Instant refreshExpiresAt
    ) {
    }
}

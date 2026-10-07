package in.aarogya.identity.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.identity.service.AuthCookieService;
import in.aarogya.identity.service.AuthService;
import in.aarogya.identity.service.InvalidRefreshTokenException;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService cookieService;

    public AuthController(
        AuthService authService,
        AuthCookieService cookieService
    ) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletResponse response
    ) {
        var session = authService.register(request);
        cookieService.writeSessionCookies(response, session);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            new AuthResponse(session.user(), session.accessExpiresAt())
        );
    }

    @PostMapping("/login")
    AuthResponse login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse response
    ) {
        var session = authService.login(request);
        cookieService.writeSessionCookies(response, session);

        return new AuthResponse(session.user(), session.accessExpiresAt());
    }

    @PostMapping("/refresh")
    AuthResponse refresh(
        @CookieValue(
            name = AuthCookieService.REFRESH_COOKIE,
            required = false
        ) String refreshToken,
        HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        var session = authService.refresh(refreshToken);
        cookieService.writeSessionCookies(response, session);

        return new AuthResponse(session.user(), session.accessExpiresAt());
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(
        @CookieValue(
            name = AuthCookieService.REFRESH_COOKIE,
            required = false
        ) String refreshToken,
        HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        cookieService.clearSessionCookies(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    UserResponse me(@AuthenticationPrincipal AarogyaPrincipal principal) {
        var user = authService.requireUser(principal.id());
        return UserResponse.from(user);
    }
}

package in.aarogya.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ACCESS_COOKIE = "aarogya_access";

    private final JwtService jwtService;
    private final AarogyaUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
        JwtService jwtService,
        AarogyaUserDetailsService userDetailsService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            var token = resolveToken(request);

            if (token != null) {
                try {
                    var claims = jwtService.parseAccessToken(token);
                    var principal = userDetailsService.loadById(
                        jwtService.subjectAsUserId(claims)
                    );

                    if (principal.isEnabled()) {
                        var authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (JwtException | IllegalArgumentException ignored) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        var cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
            .filter(cookie -> ACCESS_COOKIE.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}

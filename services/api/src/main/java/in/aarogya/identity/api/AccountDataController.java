package in.aarogya.identity.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.identity.service.AccountDataService;
import in.aarogya.identity.service.AuthCookieService;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountDataController {

    private final AccountDataService accountDataService;
    private final AuthCookieService cookieService;

    public AccountDataController(
        AccountDataService accountDataService,
        AuthCookieService cookieService
    ) {
        this.accountDataService = accountDataService;
        this.cookieService = cookieService;
    }

    @GetMapping("/export")
    ResponseEntity<Map<String, Object>> export(
        @AuthenticationPrincipal AarogyaPrincipal principal
    ) {
        return ResponseEntity.ok()
            .header(
                "Content-Disposition",
                "attachment; filename=aarogya-personal-data.json"
            )
            .body(accountDataService.export(principal.id()));
    }

    @DeleteMapping
    ResponseEntity<Void> delete(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody DeleteAccountRequest request,
        HttpServletResponse response
    ) {
        accountDataService.delete(
            principal.id(),
            request.password()
        );
        cookieService.clearSessionCookies(response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

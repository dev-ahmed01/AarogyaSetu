package in.aarogya.profile.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.aarogya.profile.service.ProfileService;
import in.aarogya.security.AarogyaPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    ProfileResponse get(@AuthenticationPrincipal AarogyaPrincipal principal) {
        return profileService.getProfile(principal.id());
    }

    @PutMapping
    ProfileResponse update(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return profileService.updateProfile(principal.id(), request);
    }

    @PutMapping("/consent")
    ProfileResponse consent(
        @AuthenticationPrincipal AarogyaPrincipal principal,
        @Valid @RequestBody ConsentUpdateRequest request
    ) {
        return profileService.recordConsent(principal.id(), request);
    }

    @PostMapping("/complete")
    ProfileResponse complete(@AuthenticationPrincipal AarogyaPrincipal principal) {
        return profileService.completeOnboarding(principal.id());
    }
}

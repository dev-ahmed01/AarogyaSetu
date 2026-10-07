package in.aarogya.common.api;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiStatusController {

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
            "service", "aarogya-api",
            "status", "ok",
            "release", "1.0.0",
            "phase", "16/16",
            "mode", "academic-research-prototype",
            "timestamp", Instant.now().toString()
        );
    }
}

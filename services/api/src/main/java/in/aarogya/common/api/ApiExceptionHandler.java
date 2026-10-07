package in.aarogya.common.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import in.aarogya.identity.service.AccountExistsException;
import in.aarogya.identity.service.InvalidRefreshTokenException;
import in.aarogya.meals.service.FoodNotLoggableException;
import in.aarogya.meals.service.MealEntryNotFoundException;
import in.aarogya.nutrition.service.FoodNotFoundException;
import in.aarogya.profile.service.ProfileIncompleteException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AccountExistsException.class)
    ResponseEntity<Map<String, Object>> accountExists(AccountExistsException exception) {
        return response(HttpStatus.CONFLICT, "ACCOUNT_EXISTS", exception.getMessage());
    }

    @ExceptionHandler({BadCredentialsException.class, InvalidRefreshTokenException.class})
    ResponseEntity<Map<String, Object>> unauthorized(RuntimeException exception) {
        return response(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", exception.getMessage());
    }

    @ExceptionHandler(ProfileIncompleteException.class)
    ResponseEntity<Map<String, Object>> profileIncomplete(ProfileIncompleteException exception) {
        return response(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "PROFILE_INCOMPLETE",
            exception.getMessage()
        );
    }

    @ExceptionHandler(FoodNotLoggableException.class)
    ResponseEntity<Map<String, Object>> foodNotLoggable(FoodNotLoggableException exception) {
        return response(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "FOOD_NOT_LOGGABLE",
            exception.getMessage()
        );
    }

    @ExceptionHandler({FoodNotFoundException.class, MealEntryNotFoundException.class})
    ResponseEntity<Map<String, Object>> notFound(RuntimeException exception) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException exception) {
        return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        var fields = new LinkedHashMap<String, String>();

        for (var error : exception.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(error.getField(), messageFor(error));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "VALIDATION_ERROR");
        body.put("message", "Please review the highlighted fields.");
        body.put("fields", fields);

        return ResponseEntity.badRequest().body(body);
    }

    private String messageFor(FieldError error) {
        return error.getDefaultMessage() == null
            ? "Invalid value"
            : error.getDefaultMessage();
    }

    private ResponseEntity<Map<String, Object>> response(
        HttpStatus status,
        String error,
        String message
    ) {
        return ResponseEntity.status(status).body(
            Map.of(
                "status", status.value(),
                "error", error,
                "message", message
            )
        );
    }
}

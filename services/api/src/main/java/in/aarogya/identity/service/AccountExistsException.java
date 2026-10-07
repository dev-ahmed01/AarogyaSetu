package in.aarogya.identity.service;

public class AccountExistsException extends RuntimeException {

    public AccountExistsException() {
        super("An account already exists for this email address.");
    }
}

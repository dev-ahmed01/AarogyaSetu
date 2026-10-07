package in.aarogya.identity.service;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("The refresh session is invalid or has expired.");
    }
}

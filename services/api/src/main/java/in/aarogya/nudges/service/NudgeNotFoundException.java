package in.aarogya.nudges.service;

public class NudgeNotFoundException extends RuntimeException {

    public NudgeNotFoundException() {
        super("Nudge was not found.");
    }
}

package in.aarogya.health.service;

public class HealthRecordNotFoundException extends RuntimeException {

    public HealthRecordNotFoundException() {
        super("Health record was not found.");
    }
}

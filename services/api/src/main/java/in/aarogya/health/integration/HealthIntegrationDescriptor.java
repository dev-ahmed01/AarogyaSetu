package in.aarogya.health.integration;

public record HealthIntegrationDescriptor(
    String providerCode,
    String displayName,
    String integrationMode,
    boolean liveConnectivity,
    String interoperabilityStandard,
    String disclaimer
) {
}

export type HealthObservation = {
  id: string;
  code: string;
  codingSystem: string;
  displayName: string;
  valueNumeric: number | null;
  valueText: string | null;
  unit: string | null;
  referenceRangeText: string | null;
  observedAt: string | null;
  sourceObservationRef: string | null;
};

export type HealthRecord = {
  id: string;
  recordType: string;
  title: string;
  summaryText: string | null;
  clinicalDate: string;
  providerName: string | null;
  facilityName: string | null;
  sourceType: "MANUAL" | "ABDM_MOCK";
  sourceSystem: string;
  sourceRecordRef: string | null;
  interoperabilityResourceType: string | null;
  verificationStatus: "SELF_REPORTED" | "MOCK_IMPORTED";
  provenanceLabel: string;
  importedAt: string | null;
  createdAt: string;
  observations: HealthObservation[];
};

export type HealthConsent = {
  purposeCode: string;
  granted: boolean;
  policyVersion: string | null;
  recordedAt: string | null;
};

export type HealthSummary = {
  recordCount: number;
  observationCount: number;
  latestClinicalDate: string | null;
  analysisConsent: HealthConsent;
};

export type HealthIntegration = {
  providerCode: string;
  displayName: string;
  integrationMode: "MOCK" | "LIVE";
  status: "CONNECTED" | "DISCONNECTED";
  liveConnectivity: boolean;
  interoperabilityStandard: string | null;
  externalSubjectRef: string | null;
  connectedAt: string | null;
  disconnectedAt: string | null;
  lastImportedAt: string | null;
  disclaimer: string;
};

export type HealthImportResult = {
  imported: number;
  skippedExisting: number;
  importedAt: string;
  sourceSystem: string;
  liveConnectivity: boolean;
  message: string;
};

export type HealthObservationInput = {
  code: string;
  displayName: string;
  valueNumeric?: number | null;
  valueText?: string | null;
  unit?: string | null;
  referenceRangeText?: string | null;
  observedAt?: string | null;
};

export type HealthRecordInput = {
  recordType: string;
  title: string;
  summaryText?: string | null;
  clinicalDate: string;
  providerName?: string | null;
  facilityName?: string | null;
  observations: HealthObservationInput[];
};

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

async function request<T>(
  path: string,
  init: RequestInit = {}
): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...init.headers
    }
  });

  if (!response.ok) {
    let message = "Could not load health records.";

    try {
      const body = (await response.json()) as { message?: string };
      if (body.message) message = body.message;
    } catch {
      // Keep fallback.
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export function getHealthSummary(): Promise<HealthSummary> {
  return request<HealthSummary>("/health/summary");
}

export function getHealthRecords(): Promise<HealthRecord[]> {
  return request<HealthRecord[]>("/health/records");
}

export function createHealthRecord(
  input: HealthRecordInput
): Promise<HealthRecord> {
  return request<HealthRecord>("/health/records", {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export function deleteHealthRecord(recordId: string): Promise<void> {
  return request<void>(`/health/records/${recordId}`, {
    method: "DELETE"
  });
}

export function updateHealthAnalysisConsent(
  granted: boolean
): Promise<HealthConsent> {
  return request<HealthConsent>("/health/consent", {
    method: "PUT",
    body: JSON.stringify({ granted })
  });
}

export function getHealthIntegration(): Promise<HealthIntegration> {
  return request<HealthIntegration>("/health/integrations/abdm/status");
}

export function connectHealthIntegration(): Promise<HealthIntegration> {
  return request<HealthIntegration>("/health/integrations/abdm/connect", {
    method: "POST"
  });
}

export function importMockHealthRecords(): Promise<HealthImportResult> {
  return request<HealthImportResult>("/health/integrations/abdm/import", {
    method: "POST"
  });
}

export function disconnectHealthIntegration(): Promise<HealthIntegration> {
  return request<HealthIntegration>("/health/integrations/abdm/connection", {
    method: "DELETE"
  });
}

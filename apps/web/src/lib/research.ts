export type ResearchConsent = {
  granted: boolean;
  policyVersion: string | null;
  recordedAt: string | null;
  instrumentationEnabled: boolean;
};

export type ResearchMetricDefinition = {
  metricCode: string;
  metricVersion: number;
  name: string;
  description: string;
  unit: string;
  windowDays: number | null;
  minimumCohortSize: number;
};

export type ResearchMetric = {
  metricCode: string;
  label: string;
  value: number | null;
  unit: string;
  eligibleParticipants: number;
  suppressed: boolean;
  interpretation: string;
};

export type ResearchExposure = {
  eventCode: string;
  participantCount: number | null;
  suppressed: boolean;
};

export type ResearchCohort = {
  segmentType: string;
  segmentValue: string;
  participantCount: number | null;
  suppressed: boolean;
};

export type ResearchOverview = {
  from: string;
  to: string;
  minimumCohortSize: number;
  optedInParticipants: number;
  metrics: ResearchMetric[];
  featureExposures: ResearchExposure[];
  dietaryPatternCohorts: ResearchCohort[];
  notices: string[];
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
    let message = "The research request could not be completed.";

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

export function getResearchConsent(): Promise<ResearchConsent> {
  return request<ResearchConsent>("/research/consent");
}

export function updateResearchConsent(
  granted: boolean
): Promise<ResearchConsent> {
  return request<ResearchConsent>("/research/consent", {
    method: "PUT",
    body: JSON.stringify({
      granted,
      policyVersion: "2026-10-research-v1"
    })
  });
}

export async function trackResearchEvent(
  eventCode: string
): Promise<void> {
  try {
    await request("/research/events", {
      method: "POST",
      body: JSON.stringify({ eventCode })
    });
  } catch {
    // Research instrumentation must never block the product workflow.
  }
}

export function getResearchOverview(
  from: string,
  to: string
): Promise<ResearchOverview> {
  return request<ResearchOverview>(
    `/admin/research/overview?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`
  );
}

export function getResearchMetricDefinitions(): Promise<
  ResearchMetricDefinition[]
> {
  return request<ResearchMetricDefinition[]>(
    "/admin/research/metrics"
  );
}

export async function downloadResearchExport(
  from: string,
  to: string
): Promise<string> {
  const response = await fetch(
    `${API_BASE}/admin/research/export?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
    {
      credentials: "include"
    }
  );

  if (!response.ok) {
    throw new Error("Could not generate the research export.");
  }

  return response.text();
}

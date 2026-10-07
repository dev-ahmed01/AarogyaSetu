export type Nudge = {
  id: string;
  ruleCode: string;
  ruleVersion: number;
  category: "SAFETY" | "NUTRITION" | "PROFILE";
  severity: "ATTENTION" | "STANDARD" | "INFO";
  status: "ACTIVE" | "SNOOZED" | "ACKNOWLEDGED" | "DISMISSED" | "RESOLVED";
  title: string;
  message: string;
  actionLabel: string | null;
  actionHref: string | null;
  reasonCode: string;
  sourceType: "RECOMMENDATION" | "HEALTH_RECORD";
  sourceRef: string | null;
  evidenceLabel: string | null;
  evidenceUrl: string | null;
  firstGeneratedAt: string;
  lastEvaluatedAt: string;
  snoozedUntil: string | null;
  acknowledgedAt: string | null;
  dismissedAt: string | null;
  resolvedAt: string | null;
};

export type NudgeSummary = {
  activeCount: number;
  attentionCount: number;
  snoozedCount: number;
};

export type NudgeEvaluation = {
  evaluationDate: string;
  signalsEvaluated: number;
  activeAfterEvaluation: number;
  resolvedDuringEvaluation: number;
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
    let message = "Could not load alerts.";

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

export function evaluateNudges(date: string): Promise<NudgeEvaluation> {
  return request<NudgeEvaluation>(
    `/nudges/evaluate?date=${encodeURIComponent(date)}`,
    { method: "POST" }
  );
}

export function getNudges(includeHistory = false): Promise<Nudge[]> {
  return request<Nudge[]>(
    `/nudges?includeHistory=${includeHistory ? "true" : "false"}`
  );
}

export function getNudgeSummary(): Promise<NudgeSummary> {
  return request<NudgeSummary>("/nudges/summary");
}

export function snoozeNudge(
  nudgeId: string,
  hours: number
): Promise<Nudge> {
  return request<Nudge>(`/nudges/${nudgeId}/snooze`, {
    method: "PUT",
    body: JSON.stringify({ hours })
  });
}

export function acknowledgeNudge(nudgeId: string): Promise<Nudge> {
  return request<Nudge>(`/nudges/${nudgeId}/acknowledge`, {
    method: "PUT"
  });
}

export function dismissNudge(nudgeId: string): Promise<Nudge> {
  return request<Nudge>(`/nudges/${nudgeId}/dismiss`, {
    method: "PUT"
  });
}

export function announceNudgeChange() {
  if (typeof window !== "undefined") {
    window.dispatchEvent(new Event("aarogya:nudges-changed"));
  }
}

export type RecommendationEvidence = {
  code: string;
  name: string;
  version: string | null;
  url: string;
};

export type RecommendationItem = {
  ruleCode: string;
  ruleVersion: number;
  priority: number;
  title: string;
  reasonCode: string;
  safetyClass: string;
  observation: string;
  whyItMatters: string;
  consideration: string;
  observedValue: number | null;
  referenceValue: number | null;
  unit: string | null;
  evidence: RecommendationEvidence | null;
};

export type RecommendationAssessment = {
  status: string;
  assessmentDate: string;
  analysisFrom: string;
  analysisTo: string;
  windowDays: number;
  observedDays: number;
  minimumTrendDays: number;
  recommendations: RecommendationItem[];
  notices: string[];
};

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

export async function getTodayRecommendations(
  date: string
): Promise<RecommendationAssessment> {
  const response = await fetch(
    `${API_BASE}/recommendations/today?date=${encodeURIComponent(date)}`,
    {
      credentials: "include",
      headers: {
        "Content-Type": "application/json"
      }
    }
  );

  if (!response.ok) {
    let message = "Could not load guidance.";

    try {
      const body = (await response.json()) as { message?: string };
      if (body.message) message = body.message;
    } catch {
      // Keep fallback.
    }

    throw new Error(message);
  }

  return (await response.json()) as RecommendationAssessment;
}

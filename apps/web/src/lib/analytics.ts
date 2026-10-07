export type AnalyticsCoverage = {
  windowDays: number;
  loggedDays: number;
  coveragePercent: number;
  status: "EMPTY" | "SPARSE" | "LIMITED" | "USABLE";
  label: string;
};

export type DailyNutritionPoint = {
  date: string;
  logged: boolean;
  entryCount: number;
  energyKcal: number | null;
  proteinG: number | null;
  fibreG: number | null;
};

export type NutrientTrend = {
  nutrientCode: string;
  label: string;
  unit: string;
  currentLoggedDayAverage: number | null;
  previousLoggedDayAverage: number | null;
  changePercent: number | null;
  currentObservedDays: number;
  previousObservedDays: number;
  direction: "INSUFFICIENT_DATA" | "HIGHER" | "LOWER" | "SIMILAR";
  interpretation: string;
};

export type MealPattern = {
  mealType: string;
  entryCount: number;
  daysPresent: number;
};

export type HealthTrendPoint = {
  observedAt: string;
  value: number;
  unit: string;
};

export type HealthTrend = {
  analysisConsentGranted: boolean;
  observationCode: string;
  label: string;
  sourceType: string;
  status: "CONSENT_REQUIRED" | "NO_DATA" | "AVAILABLE";
  notice: string;
  points: HealthTrendPoint[];
};

export type LongitudinalAnalytics = {
  asOf: string;
  windowDays: number;
  currentFrom: string;
  currentTo: string;
  previousFrom: string;
  previousTo: string;
  coverage: AnalyticsCoverage;
  previousCoverage: AnalyticsCoverage;
  currentEntryCount: number;
  previousEntryCount: number;
  dailyNutrition: DailyNutritionPoint[];
  nutrientTrends: NutrientTrend[];
  mealPatterns: MealPattern[];
  healthTrend: HealthTrend;
  insights: string[];
  dataQualityNotices: string[];
};

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: "include",
    headers: { "Content-Type": "application/json" }
  });

  if (!response.ok) {
    let message = "Could not load longitudinal analytics.";

    try {
      const body = (await response.json()) as { message?: string };
      if (body.message) message = body.message;
    } catch {
      // Keep fallback.
    }

    throw new Error(message);
  }

  return (await response.json()) as T;
}

export function getLongitudinalAnalytics(
  date: string,
  windowDays: 7 | 30
): Promise<LongitudinalAnalytics> {
  return request<LongitudinalAnalytics>(
    `/analytics/longitudinal?date=${encodeURIComponent(date)}&window=${windowDays}`
  );
}

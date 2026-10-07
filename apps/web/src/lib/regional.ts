export type RegionalContext = {
  profileValue: string | null;
  stateCode: string | null;
  stateLabel: string | null;
  macroRegionCode: string | null;
  macroRegionLabel: string | null;
  supported: boolean;
  matchedRegionCodes: string[];
  basis: string;
  disclaimer: string;
};

export type LocalizedAlias = {
  locale: string;
  alias: string;
};

export type RegionalFood = {
  food: import("@/lib/nutrition").FoodSummary;
  fitScore: number;
  fitLabel: string;
  matchedRegionCode: string;
  relationship: string;
  rationale: string;
  sourceCode: string;
  localizedAliases: LocalizedAlias[];
  planningEligible: boolean;
};

export type RegionalAlternative = {
  sourceFoodSlug: string;
  alternative: import("@/lib/nutrition").FoodSummary;
  matchedRegionCode: string;
  priority: number;
  rationale: string;
  planningEligible: boolean;
};

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: "include",
    headers: { "Content-Type": "application/json" }
  });

  if (!response.ok) {
    let message = "Could not load regional context.";
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

export function getRegionalContext(): Promise<RegionalContext> {
  return request<RegionalContext>("/regional/context");
}

export function getRegionalFoods(limit = 10): Promise<RegionalFood[]> {
  return request<RegionalFood[]>(`/regional/foods?limit=${limit}`);
}

export function getRegionalAlternatives(
  slug: string
): Promise<RegionalAlternative[]> {
  return request<RegionalAlternative[]>(
    `/regional/foods/${encodeURIComponent(slug)}/alternatives`
  );
}

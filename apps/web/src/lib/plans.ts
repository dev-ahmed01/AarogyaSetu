export type PlanNutrient = {
  code: string;
  amount: number;
  unit: string;
};

export type DietPlanItem = {
  id: string;
  mealType: "BREAKFAST" | "LUNCH" | "DINNER" | "SNACK";
  displayOrder: number;
  foodSlug: string;
  foodName: string;
  dietaryClassification: string;
  portionId: string | null;
  portionLabel: string | null;
  quantityGrams: number;
  nutrientFocusCode: string | null;
  reasonCode: string;
  explanation: string;
  sourceCode: string | null;
  sourceFoodRef: string | null;
  regionalFitScore: number | null;
  regionalFitLabel: string | null;
  regionalReason: string | null;
  nutrients: PlanNutrient[];
};

export type DietPlan = {
  id: string;
  planDate: string;
  status: string;
  generationMode: string;
  engineStatus: string;
  sourceRuleCode: string | null;
  sourceRuleVersion: number | null;
  regionalContextCode: string | null;
  regionalContextLabel: string | null;
  createdAt: string;
  items: DietPlanItem[];
  notices: string[];
};

export type PlanDay = {
  date: string;
  exists: boolean;
  plan: DietPlan | null;
};

export type SmartFoodSuggestion = {
  foodSlug: string;
  foodName: string;
  category: string;
  dietaryClassification: string;
  primaryRegion: string | null;
  portionLabel: string;
  quantityGrams: number;
  nutrientFocusCode: string | null;
  focusNutrientAmount: number | null;
  focusNutrientUnit: string | null;
  reasonCode: string;
  explanation: string;
  sourceCode: string | null;
  sourceFoodRef: string | null;
  regionalFitScore: number | null;
  regionalFitLabel: string | null;
  regionalReason: string | null;
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
    let message = "Could not load your plan.";

    try {
      const body = (await response.json()) as { message?: string };
      if (body.message) message = body.message;
    } catch {
      // Keep safe fallback.
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export function getPlanDay(date: string): Promise<PlanDay> {
  return request<PlanDay>(`/plans/day?date=${encodeURIComponent(date)}`);
}

export function getSmartSuggestions(
  date: string,
  limit = 6
): Promise<SmartFoodSuggestion[]> {
  return request<SmartFoodSuggestion[]>(
    `/plans/suggestions?date=${encodeURIComponent(date)}&limit=${limit}`
  );
}

export function generatePlan(date: string): Promise<DietPlan> {
  return request<DietPlan>("/plans/generate", {
    method: "POST",
    body: JSON.stringify({ planDate: date })
  });
}

export function archivePlan(planId: string): Promise<void> {
  return request<void>(`/plans/${planId}`, {
    method: "DELETE"
  });
}

import type { FoodSummary } from "@/lib/nutrition";

export type MealNutrient = {
  code: string;
  amount: number;
  unit: string;
};

export type MealEntry = {
  id: string;
  foodSlug: string;
  foodName: string;
  mealDate: string;
  mealType: "BREAKFAST" | "LUNCH" | "DINNER" | "SNACK";
  quantityGrams: number;
  portionId: string | null;
  portionLabel: string | null;
  portionCount: number | null;
  sourceCode: string | null;
  sourceFoodRef: string | null;
  nutrientStatus: string;
  nutrients: MealNutrient[];
  createdAt: string;
  updatedAt: string;
};

export type NutrientTotal = {
  code: string;
  amount: number;
  unit: string;
};

export type DailyMealLog = {
  date: string;
  entryCount: number;
  entries: MealEntry[];
  totals: NutrientTotal[];
};

export type MealEntryInput = {
  foodSlug: string;
  mealDate: string;
  mealType: MealEntry["mealType"];
  portionId?: string | null;
  portionCount?: number | null;
  grams?: number | null;
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
    let message = "Could not update your meal log.";

    try {
      const body = (await response.json()) as { message?: string };
      if (body.message) message = body.message;
    } catch {
      // Preserve fallback.
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export function getMealDay(date: string): Promise<DailyMealLog> {
  return request<DailyMealLog>(`/meals/day?date=${encodeURIComponent(date)}`);
}

export function createMealEntry(input: MealEntryInput): Promise<MealEntry> {
  return request<MealEntry>("/meals/entries", {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export function updateMealEntry(
  id: string,
  input: MealEntryInput
): Promise<MealEntry> {
  return request<MealEntry>(`/meals/entries/${id}`, {
    method: "PUT",
    body: JSON.stringify(input)
  });
}

export function deleteMealEntry(id: string): Promise<void> {
  return request<void>(`/meals/entries/${id}`, {
    method: "DELETE"
  });
}

export function getRecentFoods(limit = 8): Promise<FoodSummary[]> {
  return request<FoodSummary[]>(`/meals/recent?limit=${limit}`);
}

export function getFavoriteFoods(): Promise<FoodSummary[]> {
  return request<FoodSummary[]>("/meals/favorites");
}

export function addFavorite(foodSlug: string): Promise<FoodSummary> {
  return request<FoodSummary>(`/meals/favorites/${encodeURIComponent(foodSlug)}`, {
    method: "PUT"
  });
}

export function removeFavorite(foodSlug: string): Promise<void> {
  return request<void>(`/meals/favorites/${encodeURIComponent(foodSlug)}`, {
    method: "DELETE"
  });
}

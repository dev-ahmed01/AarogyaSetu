export type FoodSummary = {
  slug: string;
  name: string;
  foodType: string;
  category: string;
  dietaryClassification: string;
  primaryRegion: string | null;
  nutrientStatus: string;
  loggable: boolean;
  energyKcalPer100g: number | null;
  defaultPortion: Portion | null;
};

export type Portion = {
  id: string;
  label: string;
  grams: number;
  defaultPortion: boolean;
};

export type Nutrient = {
  code: string;
  amountPer100g: number;
  unit: string;
  sourceCode: string | null;
  sourceFoodRef: string | null;
};

export type NutritionSource = {
  code: string;
  name: string;
  version: string | null;
  type: string;
  url: string | null;
  license: string | null;
  usageNote: string | null;
  retrievedOn: string | null;
};

export type FoodDetail = {
  slug: string;
  name: string;
  description: string | null;
  foodType: string;
  category: string;
  dietaryClassification: string;
  primaryRegion: string | null;
  nutrientStatus: string;
  loggable: boolean;
  aliases: string[];
  tags: string[];
  allergens: string[];
  portions: Portion[];
  nutrients: Nutrient[];
  ingredients: {
    slug: string;
    name: string;
    quantityGrams: number | null;
  }[];
  source: NutritionSource | null;
  sourceFoodRef: string | null;
};

export type FoodPage = {
  foods: FoodSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type CatalogMetadata = {
  categories: string[];
  dietaryClassifications: string[];
  regions: string[];
  nutrientStatuses: string[];
  allergenCodes: string[];
  sources: NutritionSource[];
};

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    }
  });

  if (!response.ok) {
    let message = "Could not load nutrition data.";

    try {
      const body = (await response.json()) as { message?: string };
      if (body.message) {
        message = body.message;
      }
    } catch {
      // Keep safe fallback.
    }

    throw new Error(message);
  }

  return (await response.json()) as T;
}

export function searchFoods(params: {
  q?: string;
  category?: string;
  dietary?: string;
  region?: string;
  nutrientStatus?: string;
  page?: number;
  size?: number;
}): Promise<FoodPage> {
  const query = new URLSearchParams();

  if (params.q) query.set("q", params.q);
  if (params.category) query.set("category", params.category);
  if (params.dietary) query.set("dietary", params.dietary);
  if (params.region) query.set("region", params.region);
  if (params.nutrientStatus) query.set("nutrientStatus", params.nutrientStatus);
  query.set("page", String(params.page ?? 0));
  query.set("size", String(params.size ?? 20));

  return request<FoodPage>(`/nutrition/foods?${query.toString()}`);
}

export function getFood(slug: string): Promise<FoodDetail> {
  return request<FoodDetail>(`/nutrition/foods/${encodeURIComponent(slug)}`);
}

export function getCatalogMetadata(): Promise<CatalogMetadata> {
  return request<CatalogMetadata>("/nutrition/metadata");
}

export type AdminOverview = {
  staffRole: "NUTRITIONIST" | "ADMIN";
  totalFoods: number;
  needsReview: number;
  inReview: number;
  readyToPublish: number;
  published: number;
  unpublished: number;
  sourceCount: number;
  userCount: number | null;
  mealEntryCount: number | null;
  healthRecordCount: number | null;
  auditEventCount: number | null;
};

export type AdminFoodSummary = {
  id: string;
  slug: string;
  name: string;
  primaryRegion: string | null;
  nutrientStatus: string;
  curationStatus:
    | "NEEDS_REVIEW"
    | "IN_REVIEW"
    | "READY_TO_PUBLISH"
    | "PUBLISHED"
    | "UNPUBLISHED";
  active: boolean;
  sourceCode: string | null;
  nutrientCount: number;
  portionCount: number;
  publishReady: boolean;
};

export type AdminFoodPage = {
  items: AdminFoodSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type AdminSource = {
  id: string;
  sourceCode: string;
  name: string;
  versionLabel: string | null;
  sourceType: string;
  sourceUrl: string | null;
  licenseLabel: string | null;
  usageNote: string | null;
  retrievedOn: string | null;
};

export type AdminNutrient = {
  code: string;
  amountPer100g: number;
  unit: string;
  sourceCode: string | null;
  sourceFoodRef: string | null;
};

export type AdminPortion = {
  id: string;
  label: string;
  grams: number;
  defaultPortion: boolean;
  displayOrder: number;
};

export type AdminReview = {
  id: string;
  reviewerId: string;
  reviewerName: string;
  reviewerRole: string;
  action: string;
  fromStatus: string;
  toStatus: string;
  note: string | null;
  occurredAt: string;
};

export type AdminFoodDetail = {
  summary: AdminFoodSummary;
  description: string | null;
  foodType: string;
  category: string;
  dietaryClassification: string;
  allergens: string[];
  aliases: string[];
  source: AdminSource | null;
  sourceFoodRef: string | null;
  nutrients: AdminNutrient[];
  portions: AdminPortion[];
  reviews: AdminReview[];
};

export type FoodCurationPayload = {
  sourceId: string;
  sourceFoodRef: string;
  nutrients: {
    code: string;
    amountPer100g: number;
    unit: string;
  }[];
  defaultPortionLabel: string;
  defaultPortionGrams: number;
  note: string;
};

export type CreateSourcePayload = {
  sourceCode: string;
  name: string;
  versionLabel: string;
  sourceType: string;
  sourceUrl: string;
  licenseLabel: string;
  usageNote: string;
  retrievedOn: string | null;
};

export type AuditEvent = {
  id: number;
  actorId: string | null;
  actorName: string;
  actorRole: string;
  eventType: string;
  outcome: string;
  subject: string | null;
  metadata: string | null;
  occurredAt: string;
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
    let message = "The operations request could not be completed.";

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

export function getAdminOverview(): Promise<AdminOverview> {
  return request<AdminOverview>("/admin/overview");
}

export function getAdminFoods(
  query: string,
  status: string
): Promise<AdminFoodPage> {
  const params = new URLSearchParams();
  if (query.trim()) params.set("q", query.trim());
  if (status) params.set("status", status);
  params.set("size", "30");

  return request<AdminFoodPage>(`/admin/foods?${params.toString()}`);
}

export function getAdminFood(foodId: string): Promise<AdminFoodDetail> {
  return request<AdminFoodDetail>(`/admin/foods/${foodId}`);
}

export function saveFoodCuration(
  foodId: string,
  payload: FoodCurationPayload
): Promise<AdminFoodDetail> {
  return request<AdminFoodDetail>(`/admin/foods/${foodId}/curation`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export function markFoodReady(
  foodId: string,
  note: string
): Promise<AdminFoodDetail> {
  return reviewAction(foodId, "ready", note);
}

export function publishFood(
  foodId: string,
  note: string
): Promise<AdminFoodDetail> {
  return reviewAction(foodId, "publish", note);
}

export function unpublishFood(
  foodId: string,
  note: string
): Promise<AdminFoodDetail> {
  return reviewAction(foodId, "unpublish", note);
}

export function returnFoodForChanges(
  foodId: string,
  note: string
): Promise<AdminFoodDetail> {
  return reviewAction(foodId, "return", note);
}

function reviewAction(
  foodId: string,
  action: string,
  note: string
): Promise<AdminFoodDetail> {
  return request<AdminFoodDetail>(
    `/admin/foods/${foodId}/${action}`,
    {
      method: "POST",
      body: JSON.stringify({ note })
    }
  );
}

export function getAdminSources(): Promise<AdminSource[]> {
  return request<AdminSource[]>("/admin/sources");
}

export function createAdminSource(
  payload: CreateSourcePayload
): Promise<AdminSource> {
  return request<AdminSource>("/admin/sources", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function getAdminAudit(): Promise<AuditEvent[]> {
  return request<AuditEvent[]>("/admin/audit?limit=60");
}

export type Profile = {
  ageYears: number | null;
  sexForNutrition: string | null;
  heightCm: number | null;
  weightKg: number | null;
  activityLevel: string | null;
  dietaryPattern: string | null;
  stateOrRegion: string | null;
  goals: string[];
  allergies: string[];
  healthContexts: string[];
  personalizationConsentGranted: boolean;
  consentPolicyVersion: string | null;
  consentRecordedAt: string | null;
  onboardingComplete: boolean;
  onboardingCompletedAt: string | null;
};

export type ProfileInput = {
  ageYears: number | null;
  sexForNutrition: string | null;
  heightCm: number | null;
  weightKg: number | null;
  activityLevel: string | null;
  dietaryPattern: string | null;
  stateOrRegion: string | null;
  goals: string[];
  allergies: string[];
  healthContexts: string[];
};

type ApiErrorBody = {
  message?: string;
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
    let message = "Something went wrong. Please try again.";

    try {
      const body = (await response.json()) as ApiErrorBody;
      if (body.message) {
        message = body.message;
      }
    } catch {
      // Preserve the safe fallback.
    }

    throw new Error(message);
  }

  return (await response.json()) as T;
}

export function getProfile(): Promise<Profile> {
  return request<Profile>("/profile");
}

export function updateProfile(input: ProfileInput): Promise<Profile> {
  return request<Profile>("/profile", {
    method: "PUT",
    body: JSON.stringify(input)
  });
}

export function updatePersonalizationConsent(
  granted: boolean
): Promise<Profile> {
  return request<Profile>("/profile/consent", {
    method: "PUT",
    body: JSON.stringify({
      granted,
      policyVersion: "2026-10"
    })
  });
}

export function completeOnboarding(): Promise<Profile> {
  return request<Profile>("/profile/complete", {
    method: "POST"
  });
}

export type GoalProgress = {
  id: string;
  goalCode: string;
  title: string;
  description: string;
  status: "ACTIVE" | "PAUSED" | "ARCHIVED";
  targetValue: number;
  currentValue: number;
  progressPercent: number;
  periodCode: string;
  periodStart: string;
  periodEnd: string;
  progressLabel: string;
};

export type Streak = {
  currentRunDays: number;
  longestRunDays: number;
  totalLoggingDays: number;
  todayLogged: boolean;
  lastLoggedDate: string | null;
  message: string;
};

export type Achievement = {
  code: string;
  title: string;
  description: string;
  criteriaCode: string;
  thresholdValue: number;
  earned: boolean;
  earnedAt: string | null;
  evidenceValue: number | null;
};

export type ProgressOverview = {
  weekStart: string;
  weekEnd: string;
  mealLoggingGoal: GoalProgress | null;
  streak: Streak;
  achievements: Achievement[];
  philosophy: string;
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
    let message = "Could not load progress.";

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

export function evaluateProgress(date: string): Promise<ProgressOverview> {
  return request<ProgressOverview>(
    `/progress/evaluate?date=${encodeURIComponent(date)}`,
    { method: "POST" }
  );
}

export function getProgress(date: string): Promise<ProgressOverview> {
  return request<ProgressOverview>(
    `/progress/overview?date=${encodeURIComponent(date)}`
  );
}

export function updateMealLoggingGoal(
  targetDaysPerWeek: number
): Promise<ProgressOverview> {
  return request<ProgressOverview>("/progress/goals/meal-logging", {
    method: "PUT",
    body: JSON.stringify({ targetDaysPerWeek })
  });
}

export function pauseMealLoggingGoal(): Promise<ProgressOverview> {
  return request<ProgressOverview>("/progress/goals/meal-logging/pause", {
    method: "POST"
  });
}

export function resumeMealLoggingGoal(): Promise<ProgressOverview> {
  return request<ProgressOverview>("/progress/goals/meal-logging/resume", {
    method: "POST"
  });
}

export type AuthUser = {
  id: string;
  email: string;
  displayName: string;
  role: "USER" | "NUTRITIONIST" | "ADMIN";
};

type AuthResponse = {
  user: AuthUser;
  accessExpiresAt: string;
};

type ApiErrorBody = {
  message?: string;
  fields?: Record<string, string>;
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
    let body: ApiErrorBody = {};

    try {
      body = (await response.json()) as ApiErrorBody;
    } catch {
      // Keep a safe generic message for non-JSON failures.
    }

    throw new AuthApiError(
      body.message ?? "Something went wrong. Please try again.",
      response.status,
      body.fields
    );
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export class AuthApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly fields?: Record<string, string>
  ) {
    super(message);
    this.name = "AuthApiError";
  }
}

export async function login(
  email: string,
  password: string
): Promise<AuthResponse> {
  return request<AuthResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password })
  });
}

export async function register(
  displayName: string,
  email: string,
  password: string
): Promise<AuthResponse> {
  return request<AuthResponse>("/auth/register", {
    method: "POST",
    body: JSON.stringify({ displayName, email, password })
  });
}

export async function getCurrentUser(): Promise<AuthUser> {
  return request<AuthUser>("/auth/me");
}

export async function refreshSession(): Promise<AuthResponse> {
  return request<AuthResponse>("/auth/refresh", {
    method: "POST"
  });
}

export async function logout(): Promise<void> {
  return request<void>("/auth/logout", {
    method: "POST"
  });
}

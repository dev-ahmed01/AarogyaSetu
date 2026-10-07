const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

async function errorMessage(response: Response) {
  try {
    const body = (await response.json()) as { message?: string };
    return body.message ?? "The account request could not be completed.";
  } catch {
    return "The account request could not be completed.";
  }
}

export async function exportPersonalData(): Promise<string> {
  const response = await fetch(`${API_BASE}/account/export`, {
    credentials: "include"
  });

  if (!response.ok) {
    throw new Error(await errorMessage(response));
  }

  const payload = await response.json();

  return JSON.stringify(payload, null, 2);
}

export async function deleteAccount(
  password: string
): Promise<void> {
  const response = await fetch(`${API_BASE}/account`, {
    method: "DELETE",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ password })
  });

  if (!response.ok) {
    throw new Error(await errorMessage(response));
  }
}

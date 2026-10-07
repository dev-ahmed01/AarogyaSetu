"use client";

import { useState, type FormEvent } from "react";
import { useRouter, useSearchParams } from "next/navigation";

import { AuthApiError, login } from "@/lib/auth";

export function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      await login(email, password);
      const requested = searchParams.get("next");
      const destination =
        requested && requested.startsWith("/") ? requested : "/dashboard";
      router.replace(destination);
      router.refresh();
    } catch (cause) {
      setError(
        cause instanceof AuthApiError
          ? cause.message
          : "We could not sign you in. Please try again."
      );
      setSubmitting(false);
    }
  }

  return (
    <form className="authForm" onSubmit={handleSubmit}>
      <label className="formField">
        <span className="formField__label">Email address</span>
        <input
          className="formField__control"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          placeholder="you@example.com"
          required
        />
      </label>

      <label className="formField">
        <span className="formField__label">Password</span>
        <input
          className="formField__control"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="Your password"
          required
        />
      </label>

      {error ? (
        <div className="formNotice formNotice--error" role="alert">
          {error}
        </div>
      ) : null}

      <button className="button button--primary authForm__submit" disabled={submitting}>
        {submitting ? "Signing in…" : "Sign in"}
      </button>
    </form>
  );
}

"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";

import { AuthApiError, register } from "@/lib/auth";

export function SignupForm() {
  const router = useRouter();
  const [displayName, setDisplayName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError("The passwords do not match.");
      return;
    }

    setSubmitting(true);

    try {
      await register(displayName, email, password);
      router.replace("/dashboard");
      router.refresh();
    } catch (cause) {
      setError(
        cause instanceof AuthApiError
          ? cause.message
          : "We could not create your account. Please try again."
      );
      setSubmitting(false);
    }
  }

  return (
    <form className="authForm" onSubmit={handleSubmit}>
      <label className="formField">
        <span className="formField__label">Your name</span>
        <input
          className="formField__control"
          type="text"
          autoComplete="name"
          value={displayName}
          onChange={(event) => setDisplayName(event.target.value)}
          placeholder="How should Aarogya address you?"
          minLength={2}
          maxLength={120}
          required
        />
      </label>

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
          autoComplete="new-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="At least 10 characters"
          minLength={10}
          maxLength={72}
          required
        />
        <span className="formField__hint">
          Use a password you do not reuse on another service.
        </span>
      </label>

      <label className="formField">
        <span className="formField__label">Confirm password</span>
        <input
          className="formField__control"
          type="password"
          autoComplete="new-password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          placeholder="Repeat your password"
          minLength={10}
          maxLength={72}
          required
        />
      </label>

      {error ? (
        <div className="formNotice formNotice--error" role="alert">
          {error}
        </div>
      ) : null}

      <button className="button button--primary authForm__submit" disabled={submitting}>
        {submitting ? "Creating account…" : "Create account"}
      </button>

      <p className="authForm__legal">
        Health-profile consent is intentionally handled separately in onboarding.
        Creating an account does not consent to health-data processing beyond authentication.
      </p>
    </form>
  );
}

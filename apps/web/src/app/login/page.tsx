import Link from "next/link";
import { Suspense } from "react";

import { AuthShell } from "@/components/auth-shell";
import { LoginForm } from "@/components/login-form";

export default function LoginPage() {
  return (
    <AuthShell
      eyebrow="Welcome back"
      title="Sign in to Aarogya"
      description="Continue to your private nutrition and health workspace."
      footer={
        <p>
          New to Aarogya? <Link href="/signup">Create an account</Link>
        </p>
      }
    >
      <Suspense fallback={<div className="authFormLoading">Loading sign in…</div>}>
        <LoginForm />
      </Suspense>
    </AuthShell>
  );
}

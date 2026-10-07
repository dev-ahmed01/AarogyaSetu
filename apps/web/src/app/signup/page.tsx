import Link from "next/link";

import { AuthShell } from "@/components/auth-shell";
import { SignupForm } from "@/components/signup-form";

export default function SignupPage() {
  return (
    <AuthShell
      eyebrow="Start privately"
      title="Create your Aarogya account"
      description="Authentication comes first. Health preferences and consent are handled separately in onboarding."
      footer={
        <p>
          Already have an account? <Link href="/login">Sign in</Link>
        </p>
      }
    >
      <SignupForm />
    </AuthShell>
  );
}

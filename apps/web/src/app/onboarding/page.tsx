import { AuthGate } from "@/components/auth-gate";
import { OnboardingWizard } from "@/components/onboarding-wizard";

export default function OnboardingPage() {
  return (
    <AuthGate>
      <main className="onboardingPage">
        <OnboardingWizard />
      </main>
    </AuthGate>
  );
}

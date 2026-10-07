import { AppShell } from "@/components/app-shell";
import { MealLoggerClient } from "@/components/meal-logger-client";

export default function MealsPage() {
  return (
    <AppShell>
      <MealLoggerClient />
    </AppShell>
  );
}

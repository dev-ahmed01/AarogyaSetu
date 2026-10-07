import Link from "next/link";

import { AppShell } from "@/components/app-shell";
import { PageHeader } from "@/components/page-header";
import { StatusChip, Surface } from "@/components/ui";

export default function MealsPage() {
  return (
    <AppShell>
      <main className="workspacePage">
        <PageHeader
          eyebrow="Meals"
          title="Meal logging is next. The food foundation is ready."
          description="Phase 5 establishes the catalog first so future meal entries reference canonical foods, portions and provenance rather than free-text calorie guesses."
          action={
            <Link className="button button--primary" href="/foods">
              Browse food library
            </Link>
          }
        />

        <div className="mealFoundationGrid">
          <Surface className="dashboardCard">
            <span className="cardEyebrow">Available now</span>
            <h2 className="profileSectionTitle">Search canonical foods</h2>
            <p className="dashboardTruthCopy">
              Browse aliases, portions, dietary classifications, regions and source status.
            </p>
            <StatusChip tone="positive">Phase 5 ready</StatusChip>
          </Surface>

          <Surface className="dashboardCard">
            <span className="cardEyebrow">Coming in Phase 6</span>
            <h2 className="profileSectionTitle">Log what you actually ate</h2>
            <p className="dashboardTruthCopy">
              Meal slots, quantities, portion conversion, daily totals, history and favourites arrive next.
            </p>
            <StatusChip>No fake meal entries</StatusChip>
          </Surface>
        </div>
      </main>
    </AppShell>
  );
}

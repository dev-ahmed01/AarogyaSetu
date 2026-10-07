import { AppShell } from "@/components/app-shell";
import { PageHeader } from "@/components/page-header";
import { EmptyState, LinkButton } from "@/components/ui";

export function PlaceholderPage({
  eyebrow,
  title,
  description,
  nextPhase
}: {
  eyebrow: string;
  title: string;
  description: string;
  nextPhase: string;
}) {
  return (
    <AppShell>
      <main className="workspacePage">
        <PageHeader
          eyebrow={eyebrow}
          title={title}
          description={description}
        />

        <EmptyState
          eyebrow="Foundation ready"
          title="This workspace is intentionally quiet for now."
          description={`The shell, navigation and responsive hierarchy are complete. Functional ${title.toLowerCase()} workflows arrive in ${nextPhase}.`}
          action={<LinkButton href="/dashboard">Back to today</LinkButton>}
        />
      </main>
    </AppShell>
  );
}

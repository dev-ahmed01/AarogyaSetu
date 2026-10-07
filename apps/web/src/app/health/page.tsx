import { AppShell } from "@/components/app-shell";
import { HealthRecordsClient } from "@/components/health-records-client";

export default function HealthPage() {
  return (
    <AppShell>
      <HealthRecordsClient />
    </AppShell>
  );
}

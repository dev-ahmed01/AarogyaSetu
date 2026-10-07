import { AdminConsoleClient } from "@/components/admin-console-client";
import { AppShell } from "@/components/app-shell";

export default function AdminPage() {
  return (
    <AppShell>
      <AdminConsoleClient />
    </AppShell>
  );
}

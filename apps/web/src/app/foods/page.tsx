import { AppShell } from "@/components/app-shell";
import { FoodCatalogClient } from "@/components/food-catalog-client";

export default function FoodsPage() {
  return (
    <AppShell>
      <FoodCatalogClient />
    </AppShell>
  );
}

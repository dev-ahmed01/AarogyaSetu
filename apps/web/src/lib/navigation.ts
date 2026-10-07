export type NavigationItem = {
  label: string;
  href: string;
  eyebrow?: string;
};

export const primaryNavigation: NavigationItem[] = [
  { label: "Today", href: "/dashboard" },
  { label: "Meals", href: "/meals" },
  { label: "Plans", href: "/plans" },
  { label: "Health", href: "/health" },
  { label: "Progress", href: "/progress" }
];

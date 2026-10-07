import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Aarogya — Nutrition & Preventive Health",
  description:
    "Academic research prototype for personalized nutrition and preventive-health guidance."
};

export default function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}

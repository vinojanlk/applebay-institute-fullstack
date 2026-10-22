import type { Metadata } from "next";
import { AppShell } from "@/components/AppShell";
import "./globals.css";

export const metadata: Metadata = {
  title: "AppleBay Institute | Administration",
  description: "Academic administration for AppleBay Institute.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}

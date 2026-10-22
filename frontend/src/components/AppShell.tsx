"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";

const navigation = [
  { href: "/", label: "Overview" },
  { href: "/courses", label: "Courses" },
  { href: "/batches", label: "Batches" },
  { href: "/students", label: "Students" },
  { href: "/students/new", label: "Register student" },
  { href: "/enrollments", label: "Enrollments" },
  { href: "/payments", label: "Payments" },
  { href: "/attendance", label: "Attendance" },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <div className="app-frame">
      <aside className={`sidebar ${menuOpen ? "sidebar-open" : ""}`}>
        <Link
          className="brand"
          href="/"
          onClick={() => setMenuOpen(false)}
          aria-label="AppleBay home"
        >
          <span className="brand-mark">A</span>
          <span>
            <strong>AppleBay</strong>
            <small>Institute admin</small>
          </span>
        </Link>
        <nav className="primary-nav" aria-label="Main navigation">
          {navigation.map((item) => {
            const active =
              item.href === "/"
                ? pathname === "/"
                : pathname === item.href ||
                  (item.href === "/students" &&
                    pathname.startsWith("/students/") &&
                    pathname !== "/students/new");

            return (
              <Link
                key={item.href}
                href={item.href}
                className={`nav-link ${active ? "nav-link-active" : ""}`}
                aria-current={active ? "page" : undefined}
                onClick={() => setMenuOpen(false)}
              >
                <span className="nav-indicator" aria-hidden="true" />
                {item.label}
              </Link>
            );
          })}
        </nav>
        <div className="sidebar-footer">
          <span className="status-dot" />
          Local institute workspace
        </div>
      </aside>
      {menuOpen && (
        <button
          className="mobile-overlay"
          type="button"
          aria-label="Close navigation"
          onClick={() => setMenuOpen(false)}
        />
      )}
      <div className="main-column">
        <header className="topbar">
          <button
            className="menu-toggle"
            type="button"
            aria-label={menuOpen ? "Close navigation" : "Open navigation"}
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen((open) => !open)}
          >
            <span />
            <span />
            <span />
          </button>
          <div className="topbar-title">
            <span className="topbar-kicker">APPLEBAY INSTITUTE</span>
            <span>Academic administration</span>
          </div>
          <div className="local-label">
            <span className="status-dot" /> Local workspace
          </div>
        </header>
        <main className="main-content">{children}</main>
        <footer className="site-footer">
          <span>AppleBay Institute</span>
          <span>Student administration workspace</span>
        </footer>
      </div>
    </div>
  );
}

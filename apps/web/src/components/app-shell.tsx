"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState, type ReactNode } from "react";

import { AuthGate, useAuth } from "@/components/auth-gate";
import { primaryNavigation } from "@/lib/navigation";
import { getNudgeSummary } from "@/lib/nudges";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <AuthGate>
      <AppShellInner>{children}</AppShellInner>
    </AuthGate>
  );
}

function AppShellInner({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const { user, signOut } = useAuth();
  const [alertCount, setAlertCount] = useState(0);

  useEffect(() => {
    let active = true;

    const load = () => {
      getNudgeSummary()
        .then((summary) => {
          if (active) setAlertCount(summary.activeCount);
        })
        .catch(() => {
          if (active) setAlertCount(0);
        });
    };

    load();
    window.addEventListener("aarogya:nudges-changed", load);

    return () => {
      active = false;
      window.removeEventListener("aarogya:nudges-changed", load);
    };
  }, [pathname]);

  const initials = user.displayName
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");

  return (
    <div className="appShell">
      <header className="appTopbar">
        <Link className="brand" href="/" aria-label="Aarogya home">
          <span className="brandMark" aria-hidden="true">A</span>
          <span>Aarogya</span>
        </Link>

        <div className="appTopbar__meta">
          {user.role !== "USER" ? (
            <Link
              className={
                pathname === "/admin"
                  ? "operationsTopbarLink is-active"
                  : "operationsTopbarLink"
              }
              href="/admin"
            >
              Operations
            </Link>
          ) : null}
          {user.role === "ADMIN" ? (
            <Link
              className={
                pathname === "/research"
                  ? "operationsTopbarLink is-active"
                  : "operationsTopbarLink"
              }
              href="/research"
            >
              Research
            </Link>
          ) : null}
          <Link className="alertTopbarLink" href="/alerts">
            <span>Alerts</span>
            {alertCount > 0 ? <strong>{alertCount > 9 ? "9+" : alertCount}</strong> : null}
          </Link>
          <span className="accountName">{user.displayName}</span>
          <button className="signOutButton" type="button" onClick={() => void signOut()}>
            Sign out
          </button>
          <Link
            className="profileButton"
            href="/profile"
            aria-label={`Open profile for ${user.displayName}`}
          >
            {initials || "A"}
          </Link>
        </div>
      </header>

      <aside className="sideNav" aria-label="Aarogya workspace">
        <div className="sideNav__label">Your health</div>
        <nav className="sideNav__links">
          {primaryNavigation.map((item) => {
            const active =
              pathname === item.href
              || (item.href === "/progress" && pathname === "/analytics");

            return (
              <Link
                aria-current={active ? "page" : undefined}
                className={active ? "sideNav__link is-active" : "sideNav__link"}
                href={item.href}
                key={item.href}
              >
                <span>{item.label}</span>
                {active ? <span className="sideNav__dot" aria-hidden="true" /> : null}
              </Link>
            );
          })}
        </nav>

        <div className="sideNav__footer">
          <Link className="sideNav__profileLink" href="/profile">
            Profile & privacy
          </Link>
          <p>Your health context stays visible and controllable.</p>
        </div>
      </aside>

      <nav className="mobileNav" aria-label="Aarogya workspace mobile">
        {primaryNavigation.map((item) => {
          const active =
            pathname === item.href
            || (item.href === "/progress" && pathname === "/analytics");

          return (
            <Link
              aria-current={active ? "page" : undefined}
              className={active ? "mobileNav__link is-active" : "mobileNav__link"}
              href={item.href}
              key={item.href}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="appContent">{children}</div>
    </div>
  );
}

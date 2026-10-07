"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";

import { primaryNavigation } from "@/lib/navigation";

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();

  return (
    <div className="appShell">
      <header className="appTopbar">
        <Link className="brand" href="/" aria-label="Aarogya home">
          <span className="brandMark" aria-hidden="true">
            A
          </span>
          <span>Aarogya</span>
        </Link>

        <div className="appTopbar__meta">
          <span className="researchPill">Research prototype</span>
          <button className="profileButton" type="button" aria-label="Profile">
            AM
          </button>
        </div>
      </header>

      <aside className="sideNav" aria-label="Aarogya workspace">
        <div className="sideNav__label">Your health</div>
        <nav className="sideNav__links">
          {primaryNavigation.map((item) => {
            const active = pathname === item.href;

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
          <span className="sideNav__footerTitle">Privacy first</span>
          <p>Your health context stays visible and controllable.</p>
        </div>
      </aside>

      <nav className="mobileNav" aria-label="Aarogya workspace mobile">
        {primaryNavigation.map((item) => {
          const active = pathname === item.href;

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

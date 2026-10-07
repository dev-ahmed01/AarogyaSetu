import Link from "next/link";
import type { ReactNode } from "react";

export function AuthShell({
  eyebrow,
  title,
  description,
  children,
  footer
}: {
  eyebrow: string;
  title: string;
  description: string;
  children: ReactNode;
  footer: ReactNode;
}) {
  return (
    <main className="authPage">
      <section className="authStory">
        <Link className="brand" href="/" aria-label="Aarogya home">
          <span className="brandMark" aria-hidden="true">A</span>
          <span>Aarogya</span>
        </Link>

        <div className="authStory__content">
          <span className="sectionLabel">Private by design</span>
          <h1>Your health context should feel personal, not exposed.</h1>
          <p>
            Authentication is the boundary before nutrition history, health
            context and future records become personal to you.
          </p>
        </div>

        <p className="authStory__note">
          Academic research prototype · Not medical diagnosis
        </p>
      </section>

      <section className="authPanel">
        <div className="authCard">
          <span className="pageHeader__eyebrow">{eyebrow}</span>
          <h2>{title}</h2>
          <p className="authCard__description">{description}</p>
          {children}
          <div className="authCard__footer">{footer}</div>
        </div>
      </section>
    </main>
  );
}

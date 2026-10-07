import Link from "next/link";

import { LinkButton, StatusChip } from "@/components/ui";

const pillars = [
  {
    number: "01",
    title: "Understand your day",
    body: "See only the nutrition signals that matter now, instead of a wall of health metrics."
  },
  {
    number: "02",
    title: "Know why",
    body: "Recommendations are designed to expose their reason and context rather than hide behind an AI label."
  },
  {
    number: "03",
    title: "Stay in control",
    body: "Health context, consent and future record connections are treated as explicit user choices."
  }
];

export default function HomePage() {
  return (
    <main className="marketingPage">
      <nav className="marketingNav" aria-label="Primary navigation">
        <Link className="brand" href="/" aria-label="Aarogya home">
          <span className="brandMark" aria-hidden="true">
            A
          </span>
          <span>Aarogya</span>
        </Link>

        <div className="marketingNav__links">
          <a href="#approach">Approach</a>
          <a href="#principles">Principles</a>
          <LinkButton href="/dashboard" variant="primary">
            Open prototype
          </LinkButton>
        </div>
      </nav>

      <section className="marketingHero">
        <div className="marketingHero__inner">
          <StatusChip tone="positive">Academic research prototype</StatusChip>
          <h1>
            Health guidance that feels
            <span> calm, clear and useful.</span>
          </h1>
          <p>
            Aarogya explores how food habits, personal goals and health context
            can come together in one explainable nutrition experience for Indian users.
          </p>

          <div className="marketingHero__actions">
            <LinkButton href="/dashboard">Explore the interface</LinkButton>
            <a className="quietLink" href="#approach">
              See how it works
            </a>
          </div>

          <div className="marketingHero__note">
            <span>Wellness guidance</span>
            <span aria-hidden="true">·</span>
            <span>Not diagnosis</span>
            <span aria-hidden="true">·</span>
            <span>Privacy by design</span>
          </div>
        </div>
      </section>

      <section className="marketingSection" id="approach">
        <div className="sectionHeading sectionHeading--left">
          <span className="sectionLabel">A quieter interface</span>
          <h2>Your next action should be easier to see than your entire health history.</h2>
          <p>
            The product deliberately prioritizes today&apos;s context first. History,
            deeper analytics and connected records remain available without competing
            for attention on every screen.
          </p>
        </div>

        <div className="previewFrame" aria-label="Aarogya dashboard preview">
          <div className="previewFrame__rail">
            <span className="previewFrame__brand">A</span>
            <span className="previewFrame__railLine is-active" />
            <span className="previewFrame__railLine" />
            <span className="previewFrame__railLine" />
            <span className="previewFrame__railLine" />
          </div>

          <div className="previewFrame__content">
            <div className="previewFrame__header">
              <div>
                <span className="previewFrame__eyebrow">Today</span>
                <strong>Good morning.</strong>
              </div>
              <span className="previewFrame__avatar">AM</span>
            </div>

            <div className="previewFrame__focus">
              <span>1 thing needs attention</span>
              <strong>Fibre is below your daily target.</strong>
              <p>17g logged · 25g target</p>
            </div>

            <div className="previewFrame__grid">
              <div className="previewFrame__card">
                <span>Meals</span>
                <strong>2 of 4 logged</strong>
              </div>
              <div className="previewFrame__card">
                <span>Nutrition score</span>
                <strong>78 / 100</strong>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="marketingSection marketingSection--soft" id="principles">
        <div className="sectionHeading">
          <span className="sectionLabel">Product principles</span>
          <h2>Built to earn attention, not demand it.</h2>
        </div>

        <div className="pillarGrid">
          {pillars.map((pillar) => (
            <article className="pillarCard" key={pillar.number}>
              <span className="pillarCard__number">{pillar.number}</span>
              <div>
                <h3>{pillar.title}</h3>
                <p>{pillar.body}</p>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="researchStrip">
        <div>
          <span className="sectionLabel">Research translated into product</span>
          <h2>From dietary influence to an actual system users can evaluate.</h2>
        </div>
        <LinkButton href="/dashboard" variant="secondary">
          Enter workspace
        </LinkButton>
      </section>

      <footer className="marketingFooter">
        <div className="brand brand--footer">
          <span className="brandMark" aria-hidden="true">A</span>
          <span>Aarogya</span>
        </div>
        <p>
          Academic research prototype. Not affiliated with the official Aarogya Setu application.
        </p>
      </footer>
    </main>
  );
}

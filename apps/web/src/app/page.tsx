export default function HomePage() {
  return (
    <main>
      <nav className="topbar" aria-label="Primary navigation">
        <a className="brand" href="/" aria-label="Aarogya home">
          <span className="brandMark" aria-hidden="true">A</span>
          <span>Aarogya</span>
        </a>
        <span className="prototypeBadge">Research prototype</span>
      </nav>

      <section className="hero">
        <div className="heroInner">
          <span className="eyebrow">Nutrition · Preventive health · India</span>
          <h1>
            A calmer way to understand
            <span> what your health needs next.</span>
          </h1>
          <p>
            Aarogya connects food habits, wellness goals and health context to
            explainable everyday guidance—without pretending to replace a doctor
            or dietitian.
          </p>
          <div className="heroActions">
            <button className="primaryButton" type="button">
              Product foundation ready
            </button>
            <span className="supportingText">Phase 1 of 16</span>
          </div>
        </div>
      </section>

      <section className="foundation" aria-labelledby="foundation-heading">
        <div className="sectionHeading">
          <span className="eyebrow">Foundation</span>
          <h2 id="foundation-heading">Designed around what matters first.</h2>
          <p>
            Later phases will add onboarding, meal intelligence, health records
            and recommendations on top of this system.
          </p>
        </div>

        <div className="principleGrid">
          <article className="principleCard">
            <span className="cardKicker">01</span>
            <h3>Explainable</h3>
            <p>Every recommendation should tell the user why it exists.</p>
          </article>
          <article className="principleCard">
            <span className="cardKicker">02</span>
            <h3>Personal</h3>
            <p>Food guidance adapts to goals, preferences and relevant context.</p>
          </article>
          <article className="principleCard">
            <span className="cardKicker">03</span>
            <h3>Responsible</h3>
            <p>Wellness guidance stays clearly separated from diagnosis and treatment.</p>
          </article>
        </div>
      </section>

      <footer>
        Academic research prototype · Not affiliated with the official Aarogya Setu application.
      </footer>
    </main>
  );
}

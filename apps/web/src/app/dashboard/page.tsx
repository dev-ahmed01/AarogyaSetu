import { AppShell } from "@/components/app-shell";
import { PageHeader } from "@/components/page-header";
import { Button, ProgressBar, StatusChip, Surface } from "@/components/ui";

const meals = [
  { name: "Breakfast", detail: "Logged · 420 kcal", status: "Done" },
  { name: "Lunch", detail: "Logged · 610 kcal", status: "Done" },
  { name: "Snack", detail: "No entry yet", status: "Add" },
  { name: "Dinner", detail: "No entry yet", status: "Add" }
];

export default function DashboardPage() {
  return (
    <AppShell>
      <main className="workspacePage">
        <PageHeader
          eyebrow="Wednesday · 7 October"
          title="Good morning."
          description="Here is the small set of things worth paying attention to today."
          action={<Button variant="secondary">Log a meal</Button>}
        />

        <section className="dashboardHero" aria-label="Today's nutrition priority">
          <div>
            <StatusChip tone="warm">1 thing needs attention</StatusChip>
            <p className="dashboardHero__label">Today&apos;s focus</p>
            <h2>Fibre is below your current daily target.</h2>
            <p className="dashboardHero__copy">
              You have logged 17g so far. Your configured target is 25g.
              Suggestions will become personalized after onboarding is built.
            </p>
          </div>

          <div className="scoreBlock" aria-label="Nutrition score">
            <span className="scoreBlock__value">78</span>
            <span className="scoreBlock__divider">/</span>
            <span className="scoreBlock__total">100</span>
            <span className="scoreBlock__label">nutrition score</span>
          </div>
        </section>

        <div className="dashboardGrid">
          <Surface className="dashboardCard dashboardCard--wide">
            <div className="cardHeader">
              <div>
                <span className="cardEyebrow">Meals</span>
                <h2>Today&apos;s log</h2>
              </div>
              <button className="textAction" type="button">
                View day
              </button>
            </div>

            <div className="mealList">
              {meals.map((meal) => (
                <div className="mealRow" key={meal.name}>
                  <div>
                    <strong>{meal.name}</strong>
                    <span>{meal.detail}</span>
                  </div>
                  <button
                    className={meal.status === "Done" ? "mealStatus is-done" : "mealStatus"}
                    type="button"
                  >
                    {meal.status}
                  </button>
                </div>
              ))}
            </div>
          </Surface>

          <Surface className="dashboardCard">
            <div className="cardHeader">
              <div>
                <span className="cardEyebrow">Goals</span>
                <h2>Daily targets</h2>
              </div>
            </div>

            <div className="goalList">
              <div className="goalItem">
                <div className="goalItem__top">
                  <span>Protein</span>
                  <strong>63 / 90g</strong>
                </div>
                <ProgressBar value={70} label="Protein progress 70 percent" />
              </div>
              <div className="goalItem">
                <div className="goalItem__top">
                  <span>Fibre</span>
                  <strong>17 / 25g</strong>
                </div>
                <ProgressBar value={68} label="Fibre progress 68 percent" />
              </div>
              <div className="goalItem">
                <div className="goalItem__top">
                  <span>Water</span>
                  <strong>1.6 / 2.4L</strong>
                </div>
                <ProgressBar value={67} label="Water progress 67 percent" />
              </div>
            </div>
          </Surface>

          <Surface className="dashboardCard">
            <div className="cardHeader">
              <div>
                <span className="cardEyebrow">Health snapshot</span>
                <h2>Context, not diagnosis</h2>
              </div>
            </div>

            <div className="snapshotList">
              <div className="snapshotRow">
                <span>Weight trend</span>
                <StatusChip tone="positive">Steady</StatusChip>
              </div>
              <div className="snapshotRow">
                <span>Glucose records</span>
                <StatusChip>Not connected</StatusChip>
              </div>
              <div className="snapshotRow">
                <span>Profile completeness</span>
                <StatusChip tone="warm">40%</StatusChip>
              </div>
            </div>

            <p className="cardFootnote">
              Health-record connections and longitudinal trends arrive in later phases.
            </p>
          </Surface>
        </div>

        <section className="recommendationSection">
          <div className="sectionRow">
            <div>
              <span className="cardEyebrow">For you</span>
              <h2>Recommendations will always explain why.</h2>
            </div>
            <span className="sectionRow__note">Preview state</span>
          </div>

          <div className="recommendationGrid">
            <article className="recommendationCard">
              <span className="recommendationCard__index">01</span>
              <h3>Add one fibre-rich food</h3>
              <p>
                Based on today&apos;s logged fibre compared with your configured target.
              </p>
              <span className="recommendationCard__source">Reason visible by design</span>
            </article>
            <article className="recommendationCard">
              <span className="recommendationCard__index">02</span>
              <h3>Keep dinner simple</h3>
              <p>
                The plan engine will later suggest options that fit your remaining targets.
              </p>
              <span className="recommendationCard__source">No diagnosis · no prescription</span>
            </article>
          </div>
        </section>
      </main>
    </AppShell>
  );
}

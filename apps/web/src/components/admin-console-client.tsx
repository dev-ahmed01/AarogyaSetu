"use client";

import { FormEvent, useEffect, useState } from "react";

import { AdminAuditView } from "@/components/admin-audit-view";
import { AdminFoodReview } from "@/components/admin-food-review";
import { AdminSourcesView } from "@/components/admin-sources-view";
import { useAuth } from "@/components/auth-gate";
import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import {
  getAdminAudit,
  getAdminFood,
  getAdminFoods,
  getAdminOverview,
  getAdminSources,
  type AdminFoodDetail,
  type AdminFoodSummary,
  type AdminOverview,
  type AdminSource,
  type AuditEvent
} from "@/lib/admin";

type ConsoleView = "queue" | "sources" | "audit";

const CURATION_STATUSES = [
  "NEEDS_REVIEW",
  "IN_REVIEW",
  "READY_TO_PUBLISH",
  "PUBLISHED",
  "UNPUBLISHED"
] as const;

export function AdminConsoleClient() {
  const { user } = useAuth();
  const staff =
    user.role === "ADMIN"
    || user.role === "NUTRITIONIST";
  const admin = user.role === "ADMIN";

  const [view, setView] =
    useState<ConsoleView>("queue");
  const [overview, setOverview] =
    useState<AdminOverview | null>(null);
  const [foods, setFoods] =
    useState<AdminFoodSummary[]>([]);
  const [sources, setSources] =
    useState<AdminSource[]>([]);
  const [audit, setAudit] =
    useState<AuditEvent[]>([]);
  const [selected, setSelected] =
    useState<AdminFoodDetail | null>(null);

  const [query, setQuery] = useState("");
  const [status, setStatus] =
    useState("NEEDS_REVIEW");
  const [loading, setLoading] =
    useState(staff);
  const [detailLoading, setDetailLoading] =
    useState(false);
  const [working, setWorking] =
    useState(false);
  const [error, setError] =
    useState<string | null>(null);
  const [notice, setNotice] =
    useState<string | null>(null);

  useEffect(() => {
    if (!staff) {
      setLoading(false);
      return;
    }

    void loadInitial();
  }, [staff]);

  useEffect(() => {
    if (
      view === "audit"
      && admin
      && audit.length === 0
    ) {
      getAdminAudit()
        .then(setAudit)
        .catch((cause) =>
          setError(messageOf(cause))
        );
    }
  }, [view, admin, audit.length]);

  async function loadInitial() {
    setLoading(true);
    setError(null);

    try {
      const result = await Promise.all([
        getAdminOverview(),
        getAdminSources(),
        getAdminFoods("", "NEEDS_REVIEW")
      ]);

      setOverview(result[0]);
      setSources(result[1]);
      setFoods(result[2].items);

      if (result[2].items[0]) {
        setSelected(
          await getAdminFood(result[2].items[0].id)
        );
      }
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setLoading(false);
    }
  }

  async function refresh(
    preferredFoodId?: string
  ) {
    const result = await Promise.all([
      getAdminOverview(),
      getAdminFoods(query, status)
    ]);

    setOverview(result[0]);
    setFoods(result[1].items);

    if (
      preferredFoodId
      && result[1].items.some(
        (item) => item.id === preferredFoodId
      )
    ) {
      setSelected(
        await getAdminFood(preferredFoodId)
      );
    }
  }

  async function searchQueue(event?: FormEvent) {
    event?.preventDefault();
    setWorking(true);
    setError(null);
    setNotice(null);

    try {
      const result = await getAdminFoods(
        query,
        status
      );
      setFoods(result.items);

      if (result.items[0]) {
        setSelected(
          await getAdminFood(result.items[0].id)
        );
      } else {
        setSelected(null);
      }
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setWorking(false);
    }
  }

  async function changeStatus(next: string) {
    setStatus(next);
    setWorking(true);
    setError(null);
    setNotice(null);

    try {
      const result = await getAdminFoods(
        query,
        next
      );
      setFoods(result.items);

      if (result.items[0]) {
        setSelected(
          await getAdminFood(result.items[0].id)
        );
      } else {
        setSelected(null);
      }
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setWorking(false);
    }
  }

  async function chooseFood(foodId: string) {
    setDetailLoading(true);
    setError(null);
    setNotice(null);

    try {
      setSelected(await getAdminFood(foodId));
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setDetailLoading(false);
    }
  }

  async function onUpdated(
    detail: AdminFoodDetail,
    message: string
  ) {
    setSelected(detail);
    setNotice(message);
    setError(null);
    await refresh(detail.summary.id);

    if (admin && audit.length > 0) {
      setAudit(await getAdminAudit());
    }
  }

  if (!staff) {
    return (
      <main className="workspacePage">
        <PageHeader
          eyebrow="Operations"
          title="This workspace is staff-only."
          description="Nutrition content operations are limited to nutritionist and administrator roles. Personal health records are not exposed through this console."
        />
        <section className="adminAccessBoundary">
          <span className="cardEyebrow">
            Access boundary
          </span>
          <h2>Your account has the USER role.</h2>
          <p>
            The server enforces the same role boundary on
            every administration endpoint, so navigating
            directly to this route does not grant
            operational access.
          </p>
        </section>
      </main>
    );
  }

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Operations"
        title="Review evidence before content goes live."
        description={
          admin
            ? "Admin mode adds final publication, source registration and audit visibility. User health records remain outside this workspace."
            : "Nutritionist mode is focused on source-aware food curation and review. Final publication remains an admin responsibility."
        }
        action={
          <div
            className="adminViewSwitch"
            aria-label="Operations view"
          >
            <button
              className={
                view === "queue"
                  ? "is-active"
                  : ""
              }
              type="button"
              onClick={() => setView("queue")}
            >
              Review queue
            </button>
            <button
              className={
                view === "sources"
                  ? "is-active"
                  : ""
              }
              type="button"
              onClick={() => setView("sources")}
            >
              Sources
            </button>
            {admin ? (
              <button
                className={
                  view === "audit"
                    ? "is-active"
                    : ""
                }
                type="button"
                onClick={() => setView("audit")}
              >
                Audit
              </button>
            ) : null}
          </div>
        }
      />

      {error ? (
        <div
          className="formNotice formNotice--error"
          role="alert"
        >
          {error}
        </div>
      ) : null}

      {notice ? (
        <div
          className="formNotice formNotice--success"
          role="status"
        >
          {notice}
        </div>
      ) : null}

      {loading || !overview ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">
            A
          </div>
          <p>Loading operations workspace…</p>
        </div>
      ) : (
        <>
          <section className="adminSummaryStrip">
            <Summary
              label="Needs review"
              value={overview.needsReview}
            />
            <Summary
              label="In review"
              value={overview.inReview}
            />
            <Summary
              label="Ready to publish"
              value={overview.readyToPublish}
            />
            <Summary
              label="Published"
              value={overview.published}
            />
          </section>

          {view === "queue" ? (
            <section className="adminQueueSection">
              <form
                className="adminQueueToolbar"
                onSubmit={(event) =>
                  void searchQueue(event)
                }
              >
                <label>
                  <span>Find food</span>
                  <input
                    value={query}
                    placeholder="Name, slug or region"
                    onChange={(event) =>
                      setQuery(event.target.value)
                    }
                  />
                </label>

                <label>
                  <span>Review state</span>
                  <select
                    value={status}
                    onChange={(event) =>
                      void changeStatus(
                        event.target.value
                      )
                    }
                  >
                    {CURATION_STATUSES.map(
                      (item) => (
                        <option
                          key={item}
                          value={item}
                        >
                          {pretty(item)}
                        </option>
                      )
                    )}
                  </select>
                </label>

                <button
                  className="button button--secondary"
                  disabled={working}
                  type="submit"
                >
                  Search queue
                </button>
              </form>

              <div className="adminReviewWorkspace">
                <div className="adminFoodQueue">
                  <div className="adminFoodQueue__heading">
                    <span className="cardEyebrow">
                      {pretty(status)}
                    </span>
                    <strong>
                      {foods.length + " shown"}
                    </strong>
                  </div>

                  {foods.length === 0 ? (
                    <div className="adminQueueEmpty">
                      No foods match this review state.
                    </div>
                  ) : (
                    foods.map((food) => (
                      <button
                        className={
                          selected?.summary.id
                            === food.id
                            ? "adminFoodQueueItem is-active"
                            : "adminFoodQueueItem"
                        }
                        key={food.id}
                        type="button"
                        onClick={() =>
                          void chooseFood(food.id)
                        }
                      >
                        <div>
                          <strong>{food.name}</strong>
                          <span>
                            {(food.primaryRegion
                              ?? "No region")
                              + " · "
                              + (food.sourceCode
                                ?? "No source")}
                          </span>
                        </div>
                        <StatusChip
                          tone={statusTone(
                            food.curationStatus
                          )}
                        >
                          {pretty(
                            food.curationStatus
                          )}
                        </StatusChip>
                      </button>
                    ))
                  )}
                </div>

                <div className="adminFoodDetail">
                  {detailLoading ? (
                    <div className="dashboardLoading">
                      <p>Loading review detail…</p>
                    </div>
                  ) : selected ? (
                    <AdminFoodReview
                      admin={admin}
                      detail={selected}
                      sources={sources}
                      working={working}
                      setWorking={setWorking}
                      onUpdated={onUpdated}
                      onError={setError}
                    />
                  ) : (
                    <div className="adminQueueEmpty">
                      Choose a food from the queue
                      to review it.
                    </div>
                  )}
                </div>
              </div>
            </section>
          ) : null}

          {view === "sources" ? (
            <AdminSourcesView
              admin={admin}
              sources={sources}
              working={working}
              setWorking={setWorking}
              onSourcesChanged={setSources}
              onNotice={setNotice}
              onError={setError}
            />
          ) : null}

          {view === "audit" && admin ? (
            <AdminAuditView events={audit} />
          ) : null}
        </>
      )}
    </main>
  );
}

function Summary({
  label,
  value
}: {
  label: string;
  value: number;
}) {
  return (
    <div>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function statusTone(
  status: string
): "neutral" | "positive" | "warm" {
  if (
    status === "PUBLISHED"
    || status === "READY_TO_PUBLISH"
  ) {
    return "positive";
  }

  if (
    status === "NEEDS_REVIEW"
    || status === "IN_REVIEW"
  ) {
    return "warm";
  }

  return "neutral";
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) =>
      letter.toUpperCase()
    );
}

function messageOf(cause: unknown) {
  return cause instanceof Error
    ? cause.message
    : "The operations request could not be completed.";
}

"use client";

import { FormEvent, useState } from "react";

import { StatusChip } from "@/components/ui";
import {
  createAdminSource,
  type AdminSource,
  type CreateSourcePayload
} from "@/lib/admin";

export function AdminSourcesView({
  admin,
  sources,
  working,
  setWorking,
  onSourcesChanged,
  onNotice,
  onError
}: {
  admin: boolean;
  sources: AdminSource[];
  working: boolean;
  setWorking: (value: boolean) => void;
  onSourcesChanged: (sources: AdminSource[]) => void;
  onNotice: (message: string) => void;
  onError: (message: string) => void;
}) {
  const [draft, setDraft] = useState<CreateSourcePayload>({
    sourceCode: "",
    name: "",
    versionLabel: "",
    sourceType: "FOOD_COMPOSITION",
    sourceUrl: "",
    licenseLabel: "",
    usageNote: "",
    retrievedOn: null
  });

  async function create(event: FormEvent) {
    event.preventDefault();
    if (!admin) return;

    setWorking(true);

    try {
      const created = await createAdminSource(draft);
      onSourcesChanged(
        [...sources, created].sort((a, b) =>
          a.name.localeCompare(b.name)
        )
      );
      setDraft({
        sourceCode: "",
        name: "",
        versionLabel: "",
        sourceType: "FOOD_COMPOSITION",
        sourceUrl: "",
        licenseLabel: "",
        usageNote: "",
        retrievedOn: null
      });
      onNotice("Provenance source registered.");
    } catch (cause) {
      onError(
        cause instanceof Error
          ? cause.message
          : "Could not register the source."
      );
    } finally {
      setWorking(false);
    }
  }

  return (
    <section className="adminSourcesWorkspace">
      <div className="adminSourceRegistry">
        <div className="adminSectionHeading">
          <div>
            <span className="cardEyebrow">
              Provenance registry
            </span>
            <h2>Known nutrition sources</h2>
          </div>
          <span>{sources.length + " sources"}</span>
        </div>

        <div className="adminSourceList">
          {sources.map((source) => (
            <article key={source.id}>
              <div>
                <strong>{source.name}</strong>
                <span>{source.sourceCode}</span>
              </div>

              <StatusChip
                tone={
                  source.sourceType === "FOOD_COMPOSITION"
                    ? "positive"
                    : "neutral"
                }
              >
                {pretty(source.sourceType)}
              </StatusChip>

              <p>
                {(source.versionLabel ?? "No version label")
                  + " · "
                  + (source.licenseLabel
                    ?? "No license label recorded")}
              </p>
            </article>
          ))}
        </div>
      </div>

      <aside className="adminSourceCreate">
        <span className="cardEyebrow">
          {admin ? "Admin action" : "Read-only"}
        </span>
        <h2>
          {admin
            ? "Register a provenance source"
            : "Source creation is admin-only"}
        </h2>
        <p>
          A source record never makes a food publishable on
          its own. Publishable nutrition still requires a
          food-composition source, record reference, core
          nutrients and a portion.
        </p>

        {admin ? (
          <form onSubmit={(event) => void create(event)}>
            <label>
              <span>Source code</span>
              <input
                required
                value={draft.sourceCode}
                onChange={(event) =>
                  setDraft({
                    ...draft,
                    sourceCode: event.target.value
                  })
                }
                placeholder="INDIAN_RECIPE_DATA_V1"
              />
            </label>

            <label>
              <span>Name</span>
              <input
                required
                value={draft.name}
                onChange={(event) =>
                  setDraft({
                    ...draft,
                    name: event.target.value
                  })
                }
              />
            </label>

            <label>
              <span>Type</span>
              <select
                value={draft.sourceType}
                onChange={(event) =>
                  setDraft({
                    ...draft,
                    sourceType: event.target.value
                  })
                }
              >
                <option value="FOOD_COMPOSITION">
                  Food composition
                </option>
                <option value="EDITORIAL">
                  Editorial
                </option>
                <option value="GUIDANCE_REFERENCE">
                  Guidance reference
                </option>
                <option value="REGULATORY_REFERENCE">
                  Regulatory reference
                </option>
              </select>
            </label>

            <label>
              <span>Version</span>
              <input
                value={draft.versionLabel}
                onChange={(event) =>
                  setDraft({
                    ...draft,
                    versionLabel: event.target.value
                  })
                }
              />
            </label>

            <label>
              <span>Source URL</span>
              <input
                value={draft.sourceUrl}
                onChange={(event) =>
                  setDraft({
                    ...draft,
                    sourceUrl: event.target.value
                  })
                }
              />
            </label>

            <label>
              <span>License label</span>
              <input
                value={draft.licenseLabel}
                onChange={(event) =>
                  setDraft({
                    ...draft,
                    licenseLabel: event.target.value
                  })
                }
              />
            </label>

            <label>
              <span>Usage note</span>
              <textarea
                rows={3}
                value={draft.usageNote}
                onChange={(event) =>
                  setDraft({
                    ...draft,
                    usageNote: event.target.value
                  })
                }
              />
            </label>

            <button
              className="button button--primary"
              disabled={working}
              type="submit"
            >
              Register source
            </button>
          </form>
        ) : null}
      </aside>
    </section>
  );
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) =>
      letter.toUpperCase()
    );
}

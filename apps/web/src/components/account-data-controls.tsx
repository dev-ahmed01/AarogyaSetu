"use client";

import { FormEvent, useState } from "react";

import { useAuth } from "@/components/auth-gate";
import { StatusChip, Surface } from "@/components/ui";
import {
  deleteAccount,
  exportPersonalData
} from "@/lib/account";

export function AccountDataControls() {
  const { user } = useAuth();
  const [exporting, setExporting] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [showDelete, setShowDelete] = useState(false);
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  async function handleExport() {
    setExporting(true);
    setError(null);

    try {
      const json = await exportPersonalData();
      const blob = new Blob([json], {
        type: "application/json;charset=utf-8"
      });
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = "aarogya-personal-data.json";
      anchor.click();
      URL.revokeObjectURL(url);
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setExporting(false);
    }
  }

  async function handleDelete(event: FormEvent) {
    event.preventDefault();

    if (!window.confirm(
      "Delete your Aarogya account and user-owned data? This cannot be undone."
    )) {
      return;
    }

    setDeleting(true);
    setError(null);

    try {
      await deleteAccount(password);
      window.location.assign("/");
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setDeleting(false);
    }
  }

  const staff = user.role !== "USER";

  return (
    <Surface className="profileCard profileCard--wide">
      <div className="cardHeader">
        <div>
          <span className="cardEyebrow">Data controls</span>
          <h2>Export or remove your account data</h2>
        </div>
        <StatusChip tone="neutral">
          Privacy controls
        </StatusChip>
      </div>

      <div className="accountDataControlGrid">
        <section>
          <strong>Download a copy</strong>
          <p>
            Export your stored account, profile, consent, meal,
            plan, health-record, alert, progress and research-event
            data as JSON.
          </p>
          <button
            className="button button--secondary"
            type="button"
            disabled={exporting}
            onClick={() => void handleExport()}
          >
            {exporting ? "Preparing…" : "Download my data"}
          </button>
        </section>

        <section>
          <strong>Delete account</strong>
          <p>
            {staff
              ? "Staff accounts cannot be self-deleted because content-review history must remain attributable."
              : "Deletion removes the account and user-owned records through database cascades. Retained security-audit rows are anonymized first."}
          </p>

          {!staff && !showDelete ? (
            <button
              className="button button--ghost"
              type="button"
              onClick={() => setShowDelete(true)}
            >
              Start account deletion
            </button>
          ) : null}

          {!staff && showDelete ? (
            <form
              className="accountDeleteForm"
              onSubmit={(event) => void handleDelete(event)}
            >
              <label>
                <span>Confirm password</span>
                <input
                  autoComplete="current-password"
                  maxLength={72}
                  required
                  type="password"
                  value={password}
                  onChange={(event) =>
                    setPassword(event.target.value)
                  }
                />
              </label>
              <div>
                <button
                  className="button button--ghost"
                  type="button"
                  disabled={deleting}
                  onClick={() => {
                    setShowDelete(false);
                    setPassword("");
                  }}
                >
                  Cancel
                </button>
                <button
                  className="button button--secondary"
                  type="submit"
                  disabled={deleting || !password}
                >
                  {deleting
                    ? "Deleting…"
                    : "Confirm deletion"}
                </button>
              </div>
            </form>
          ) : null}
        </section>
      </div>

      {error ? (
        <div
          className="formNotice formNotice--error accountDataError"
          role="alert"
        >
          {error}
        </div>
      ) : null}
    </Surface>
  );
}

function messageOf(cause: unknown) {
  return cause instanceof Error
    ? cause.message
    : "The account request could not be completed.";
}

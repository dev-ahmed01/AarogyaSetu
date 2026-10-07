"use client";

import { useEffect, useMemo, useState } from "react";

import { PageHeader } from "@/components/page-header";
import { StatusChip } from "@/components/ui";
import {
  connectHealthIntegration,
  createHealthRecord,
  deleteHealthRecord,
  disconnectHealthIntegration,
  getHealthIntegration,
  getHealthRecords,
  getHealthSummary,
  importMockHealthRecords,
  updateHealthAnalysisConsent,
  type HealthIntegration,
  type HealthRecord,
  type HealthSummary
} from "@/lib/health";

type ManualObservation = {
  code: string;
  displayName: string;
  value: string;
  unit: string;
};

const recordTypes = [
  "LAB_REPORT",
  "PRESCRIPTION",
  "DISCHARGE_SUMMARY",
  "OP_CONSULT",
  "IMMUNIZATION",
  "MEASUREMENT_SET",
  "OTHER"
];

export function HealthRecordsClient() {
  const [summary, setSummary] = useState<HealthSummary | null>(null);
  const [records, setRecords] = useState<HealthRecord[]>([]);
  const [integration, setIntegration] = useState<HealthIntegration | null>(null);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [composing, setComposing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const [recordType, setRecordType] = useState("LAB_REPORT");
  const [title, setTitle] = useState("");
  const [clinicalDate, setClinicalDate] = useState(todayKey());
  const [providerName, setProviderName] = useState("");
  const [facilityName, setFacilityName] = useState("");
  const [summaryText, setSummaryText] = useState("");
  const [observations, setObservations] = useState<ManualObservation[]>([]);

  async function refresh() {
    setLoading(true);
    setError(null);

    try {
      const [summaryResult, recordsResult, integrationResult] = await Promise.all([
        getHealthSummary(),
        getHealthRecords(),
        getHealthIntegration()
      ]);

      setSummary(summaryResult);
      setRecords(recordsResult);
      setIntegration(integrationResult);

      if (
        selectedId &&
        !recordsResult.some((record) => record.id === selectedId)
      ) {
        setSelectedId(recordsResult[0]?.id ?? null);
      } else if (!selectedId && recordsResult.length > 0) {
        setSelectedId(recordsResult[0].id);
      }
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not load health records."
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
  }, []);

  const selected = useMemo(
    () => records.find((record) => record.id === selectedId) ?? null,
    [records, selectedId]
  );

  async function saveRecord() {
    if (!title.trim()) {
      setError("Add a title before saving this health record.");
      return;
    }

    setWorking(true);
    setError(null);
    setNotice(null);

    try {
      const observationPayload = observations
        .filter(
          (observation) =>
            observation.code.trim()
            && observation.displayName.trim()
            && observation.value.trim()
        )
        .map((observation) => {
          const numeric = Number(observation.value);
          const isNumeric = Number.isFinite(numeric);

          return {
            code: observation.code.trim(),
            displayName: observation.displayName.trim(),
            valueNumeric: isNumeric ? numeric : null,
            valueText: isNumeric ? null : observation.value.trim(),
            unit: observation.unit.trim() || null,
            observedAt: clinicalDate
              ? new Date(`${clinicalDate}T12:00:00`).toISOString()
              : null
          };
        });

      const created = await createHealthRecord({
        recordType,
        title: title.trim(),
        clinicalDate,
        providerName: providerName.trim() || null,
        facilityName: facilityName.trim() || null,
        summaryText: summaryText.trim() || null,
        observations: observationPayload
      });

      resetComposer();
      await refresh();
      setSelectedId(created.id);
      setNotice("Health record saved as self-reported local data.");
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not save this health record."
      );
    } finally {
      setWorking(false);
    }
  }

  async function removeRecord(record: HealthRecord) {
    const sourceMessage =
      record.sourceType === "ABDM_MOCK"
        ? "This removes only the local demo copy."
        : "This removes the local record.";

    if (!window.confirm(`${sourceMessage} Continue?`)) return;

    setWorking(true);
    setError(null);

    try {
      await deleteHealthRecord(record.id);
      await refresh();
      setNotice("Local health record removed.");
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not delete this health record."
      );
    } finally {
      setWorking(false);
    }
  }

  async function toggleAnalysisConsent() {
    if (!summary) return;

    setWorking(true);
    setError(null);

    try {
      const next = await updateHealthAnalysisConsent(
        !summary.analysisConsent.granted
      );
      setSummary({
        ...summary,
        analysisConsent: next
      });
      setNotice(
        next.granted
          ? "Health-record analysis consent recorded. Phase 9 still does not feed records into recommendations yet."
          : "Health-record analysis consent withdrawn."
      );
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not update health-record consent."
      );
    } finally {
      setWorking(false);
    }
  }

  async function connectDemo() {
    setWorking(true);
    setError(null);
    setNotice(null);

    try {
      setIntegration(await connectHealthIntegration());
      setNotice("Demo source connected. No live ABDM or ABHA request was made.");
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not connect the demo source."
      );
    } finally {
      setWorking(false);
    }
  }

  async function importDemo() {
    setWorking(true);
    setError(null);
    setNotice(null);

    try {
      const result = await importMockHealthRecords();
      await refresh();
      setNotice(
        `${result.message} Imported: ${result.imported}; already present: ${result.skippedExisting}.`
      );
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not import demo records."
      );
    } finally {
      setWorking(false);
    }
  }

  async function disconnectDemo() {
    setWorking(true);
    setError(null);

    try {
      setIntegration(await disconnectHealthIntegration());
      setNotice("Demo source disconnected. Existing local imported copies were retained.");
    } catch (cause) {
      setError(
        cause instanceof Error ? cause.message : "Could not disconnect the demo source."
      );
    } finally {
      setWorking(false);
    }
  }

  function resetComposer() {
    setComposing(false);
    setRecordType("LAB_REPORT");
    setTitle("");
    setClinicalDate(todayKey());
    setProviderName("");
    setFacilityName("");
    setSummaryText("");
    setObservations([]);
  }

  function addObservation() {
    setObservations((current) => [
      ...current,
      { code: "", displayName: "", value: "", unit: "" }
    ]);
  }

  function updateObservation(
    index: number,
    key: keyof ManualObservation,
    value: string
  ) {
    setObservations((current) =>
      current.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [key]: value } : item
      )
    );
  }

  function removeObservation(index: number) {
    setObservations((current) =>
      current.filter((_, itemIndex) => itemIndex !== index)
    );
  }

  return (
    <main className="workspacePage">
      <PageHeader
        eyebrow="Health"
        title="A record timeline with provenance kept visible."
        description="Store self-reported records locally, inspect structured observations, and demonstrate an ABDM-compatible integration boundary without claiming live ABHA connectivity."
        action={
          <button
            className="button button--primary"
            type="button"
            onClick={() => {
              setComposing(true);
              setSelectedId(null);
              setNotice(null);
            }}
          >
            Add health record
          </button>
        }
      />

      {error ? (
        <div className="formNotice formNotice--error" role="alert">
          {error}
        </div>
      ) : null}
      {notice ? (
        <div className="formNotice" role="status">
          {notice}
        </div>
      ) : null}

      <section className="healthSummaryStrip">
        <div>
          <span>Records</span>
          <strong>{summary?.recordCount ?? 0}</strong>
        </div>
        <div>
          <span>Observations</span>
          <strong>{summary?.observationCount ?? 0}</strong>
        </div>
        <div>
          <span>Latest clinical date</span>
          <strong>
            {summary?.latestClinicalDate
              ? formatDate(summary.latestClinicalDate)
              : "None yet"}
          </strong>
        </div>
        <div>
          <span>Record analysis</span>
          <strong>
            {summary?.analysisConsent.granted ? "Allowed" : "Paused"}
          </strong>
        </div>
      </section>

      {loading ? (
        <div className="dashboardLoading">
          <div className="sessionLoading__mark">A</div>
          <p>Loading health records…</p>
        </div>
      ) : (
        <div className="healthWorkspace">
          <section className="healthTimeline">
            <div className="healthTimeline__heading">
              <div>
                <span className="cardEyebrow">Timeline</span>
                <h2>Health records</h2>
              </div>
              <span>{records.length} stored</span>
            </div>

            {records.length > 0 ? (
              <div className="healthRecordList">
                {records.map((record) => (
                  <button
                    type="button"
                    key={record.id}
                    className={
                      selected?.id === record.id && !composing
                        ? "healthRecordRow is-selected"
                        : "healthRecordRow"
                    }
                    onClick={() => {
                      setComposing(false);
                      setSelectedId(record.id);
                    }}
                  >
                    <div>
                      <strong>{record.title}</strong>
                      <span>
                        {formatDate(record.clinicalDate)} · {pretty(record.recordType)}
                      </span>
                    </div>
                    <StatusChip tone={record.sourceType === "ABDM_MOCK" ? "warm" : "neutral"}>
                      {record.sourceType === "ABDM_MOCK" ? "Synthetic demo" : "Self-reported"}
                    </StatusChip>
                  </button>
                ))}
              </div>
            ) : (
              <div className="healthTimeline__empty">
                <p>No health records are stored yet.</p>
                <button
                  className="quietLink"
                  type="button"
                  onClick={() => setComposing(true)}
                >
                  Add the first record
                </button>
              </div>
            )}
          </section>

          <aside className="healthDetailPanel">
            {composing ? (
              <ManualRecordComposer
                recordType={recordType}
                setRecordType={setRecordType}
                title={title}
                setTitle={setTitle}
                clinicalDate={clinicalDate}
                setClinicalDate={setClinicalDate}
                providerName={providerName}
                setProviderName={setProviderName}
                facilityName={facilityName}
                setFacilityName={setFacilityName}
                summaryText={summaryText}
                setSummaryText={setSummaryText}
                observations={observations}
                addObservation={addObservation}
                updateObservation={updateObservation}
                removeObservation={removeObservation}
                onCancel={resetComposer}
                onSave={saveRecord}
                working={working}
              />
            ) : selected ? (
              <HealthRecordDetail
                record={selected}
                onDelete={() => void removeRecord(selected)}
                working={working}
              />
            ) : (
              <div className="healthDetailEmpty">
                <span className="cardEyebrow">Record details</span>
                <h2>Select a record.</h2>
                <p>
                  Structured observations and source provenance appear here without opening another dense screen.
                </p>
              </div>
            )}
          </aside>
        </div>
      )}

      <section className="healthSourceSection">
        <div className="healthSourceCard">
          <div className="healthSourceCard__top">
            <div>
              <span className="cardEyebrow">Interoperability source</span>
              <h2>{integration?.displayName ?? "ABDM / ABHA architecture demo"}</h2>
            </div>
            <StatusChip tone={integration?.status === "CONNECTED" ? "positive" : "warm"}>
              {integration?.status === "CONNECTED" ? "Demo connected" : "Disconnected"}
            </StatusChip>
          </div>

          <p className="healthSourceCard__copy">
            {integration?.disclaimer
              ?? "Synthetic local demo only. No live government network request is made."}
          </p>

          <dl className="healthSourceMeta">
            <div>
              <dt>Mode</dt>
              <dd>{integration?.integrationMode ?? "MOCK"}</dd>
            </div>
            <div>
              <dt>Live connectivity</dt>
              <dd>{integration?.liveConnectivity ? "Yes" : "No"}</dd>
            </div>
            <div>
              <dt>Interoperability</dt>
              <dd>{integration?.interoperabilityStandard ?? "FHIR-compatible boundary"}</dd>
            </div>
            <div>
              <dt>Subject reference</dt>
              <dd>{integration?.externalSubjectRef ?? "Not connected"}</dd>
            </div>
          </dl>

          <div className="healthSourceActions">
            {integration?.status === "CONNECTED" ? (
              <>
                <button
                  className="button button--primary"
                  type="button"
                  disabled={working}
                  onClick={() => void importDemo()}
                >
                  Import synthetic demo records
                </button>
                <button
                  className="button button--ghost"
                  type="button"
                  disabled={working}
                  onClick={() => void disconnectDemo()}
                >
                  Disconnect demo
                </button>
              </>
            ) : (
              <button
                className="button button--primary"
                type="button"
                disabled={working}
                onClick={() => void connectDemo()}
              >
                Connect demo source
              </button>
            )}
          </div>
        </div>

        <div className="healthConsentCard">
          <span className="cardEyebrow">Separate consent</span>
          <h2>Use health records for future wellness guidance?</h2>
          <p>
            This consent is independent from importing/storing records. Phase 9 records the choice, but the current recommendation engine does not read these health records yet.
          </p>

          <div className="healthConsentState">
            <StatusChip tone={summary?.analysisConsent.granted ? "positive" : "neutral"}>
              {summary?.analysisConsent.granted ? "Allowed" : "Not allowed"}
            </StatusChip>
            <button
              className="button button--secondary"
              type="button"
              disabled={working || !summary}
              onClick={() => void toggleAnalysisConsent()}
            >
              {summary?.analysisConsent.granted ? "Withdraw consent" : "Allow analysis"}
            </button>
          </div>
        </div>
      </section>
    </main>
  );
}

function ManualRecordComposer({
  recordType,
  setRecordType,
  title,
  setTitle,
  clinicalDate,
  setClinicalDate,
  providerName,
  setProviderName,
  facilityName,
  setFacilityName,
  summaryText,
  setSummaryText,
  observations,
  addObservation,
  updateObservation,
  removeObservation,
  onCancel,
  onSave,
  working
}: {
  recordType: string;
  setRecordType: (value: string) => void;
  title: string;
  setTitle: (value: string) => void;
  clinicalDate: string;
  setClinicalDate: (value: string) => void;
  providerName: string;
  setProviderName: (value: string) => void;
  facilityName: string;
  setFacilityName: (value: string) => void;
  summaryText: string;
  setSummaryText: (value: string) => void;
  observations: ManualObservation[];
  addObservation: () => void;
  updateObservation: (
    index: number,
    key: keyof ManualObservation,
    value: string
  ) => void;
  removeObservation: (index: number) => void;
  onCancel: () => void;
  onSave: () => Promise<void>;
  working: boolean;
}) {
  return (
    <div className="healthComposer">
      <div className="healthDetailHeading">
        <div>
          <span className="cardEyebrow">Self-reported record</span>
          <h2>Add health record</h2>
        </div>
        <button className="textAction" type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>

      <div className="healthFormGrid">
        <label className="formField">
          <span className="formField__label">Record type</span>
          <select
            className="formField__control"
            value={recordType}
            onChange={(event) => setRecordType(event.target.value)}
          >
            {recordTypes.map((type) => (
              <option key={type} value={type}>
                {pretty(type)}
              </option>
            ))}
          </select>
        </label>

        <label className="formField">
          <span className="formField__label">Clinical date</span>
          <input
            className="formField__control"
            type="date"
            max={todayKey()}
            value={clinicalDate}
            onChange={(event) => setClinicalDate(event.target.value)}
          />
        </label>
      </div>

      <label className="formField">
        <span className="formField__label">Title</span>
        <input
          className="formField__control"
          value={title}
          maxLength={220}
          onChange={(event) => setTitle(event.target.value)}
          placeholder="e.g. Annual lab report"
        />
      </label>

      <div className="healthFormGrid">
        <label className="formField">
          <span className="formField__label">Provider</span>
          <input
            className="formField__control"
            value={providerName}
            onChange={(event) => setProviderName(event.target.value)}
            placeholder="Optional"
          />
        </label>
        <label className="formField">
          <span className="formField__label">Facility</span>
          <input
            className="formField__control"
            value={facilityName}
            onChange={(event) => setFacilityName(event.target.value)}
            placeholder="Optional"
          />
        </label>
      </div>

      <label className="formField">
        <span className="formField__label">Summary</span>
        <textarea
          className="formField__control healthTextarea"
          value={summaryText}
          maxLength={1600}
          onChange={(event) => setSummaryText(event.target.value)}
          placeholder="Optional note. Do not use this field as a diagnosis generated by Aarogya."
        />
      </label>

      <div className="manualObservationSection">
        <div className="manualObservationSection__heading">
          <div>
            <span className="formField__label">Structured observations</span>
            <small>Optional</small>
          </div>
          <button className="textAction" type="button" onClick={addObservation}>
            + Add measurement
          </button>
        </div>

        {observations.map((observation, index) => (
          <div className="manualObservationRow" key={index}>
            <input
              value={observation.displayName}
              onChange={(event) =>
                updateObservation(index, "displayName", event.target.value)
              }
              placeholder="Display name"
            />
            <input
              value={observation.code}
              onChange={(event) =>
                updateObservation(index, "code", event.target.value)
              }
              placeholder="Code"
            />
            <input
              value={observation.value}
              onChange={(event) =>
                updateObservation(index, "value", event.target.value)
              }
              placeholder="Value"
            />
            <input
              value={observation.unit}
              onChange={(event) =>
                updateObservation(index, "unit", event.target.value)
              }
              placeholder="Unit"
            />
            <button
              type="button"
              aria-label="Remove measurement"
              onClick={() => removeObservation(index)}
            >
              ×
            </button>
          </div>
        ))}
      </div>

      <button
        className="button button--primary healthSaveButton"
        type="button"
        disabled={working}
        onClick={() => void onSave()}
      >
        {working ? "Saving…" : "Save local record"}
      </button>
    </div>
  );
}

function HealthRecordDetail({
  record,
  onDelete,
  working
}: {
  record: HealthRecord;
  onDelete: () => void;
  working: boolean;
}) {
  return (
    <div className="healthRecordDetail">
      <div className="healthDetailHeading">
        <div>
          <span className="cardEyebrow">{pretty(record.recordType)}</span>
          <h2>{record.title}</h2>
        </div>
        <StatusChip tone={record.sourceType === "ABDM_MOCK" ? "warm" : "neutral"}>
          {record.sourceType === "ABDM_MOCK" ? "Synthetic demo" : "Self-reported"}
        </StatusChip>
      </div>

      <p className="healthRecordDetail__summary">
        {record.summaryText ?? "No summary was stored for this record."}
      </p>

      <dl className="healthRecordMeta">
        <div>
          <dt>Clinical date</dt>
          <dd>{formatDate(record.clinicalDate)}</dd>
        </div>
        <div>
          <dt>Provider</dt>
          <dd>{record.providerName ?? "Not provided"}</dd>
        </div>
        <div>
          <dt>Facility</dt>
          <dd>{record.facilityName ?? "Not provided"}</dd>
        </div>
        <div>
          <dt>Verification state</dt>
          <dd>{pretty(record.verificationStatus)}</dd>
        </div>
      </dl>

      <section className="healthObservationList">
        <div className="healthObservationList__heading">
          <h3>Structured observations</h3>
          <span>{record.observations.length}</span>
        </div>

        {record.observations.length > 0 ? (
          record.observations.map((observation) => (
            <div className="healthObservationRow" key={observation.id}>
              <div>
                <strong>{observation.displayName}</strong>
                <span>{observation.code} · {observation.codingSystem}</span>
              </div>
              <div>
                <strong>
                  {observation.valueNumeric !== null
                    ? formatNumber(observation.valueNumeric)
                    : observation.valueText}
                </strong>
                <span>{observation.unit ?? ""}</span>
              </div>
            </div>
          ))
        ) : (
          <p className="healthObservationEmpty">
            This record has no structured measurements.
          </p>
        )}
      </section>

      <section className="healthProvenance">
        <span>Provenance</span>
        <strong>{record.sourceSystem}</strong>
        <p>{record.provenanceLabel}</p>
        {record.interoperabilityResourceType ? (
          <small>
            Resource boundary: {record.interoperabilityResourceType}
            {record.sourceRecordRef ? ` · ${record.sourceRecordRef}` : ""}
          </small>
        ) : null}
      </section>

      <button
        className="textAction healthDeleteAction"
        type="button"
        disabled={working}
        onClick={onDelete}
      >
        Delete local copy
      </button>
    </div>
  );
}

function pretty(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric"
  }).format(new Date(`${value}T12:00:00`));
}

function formatNumber(value: number) {
  return new Intl.NumberFormat("en-IN", {
    maximumFractionDigits: 2
  }).format(value);
}

function todayKey() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

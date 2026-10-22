"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiErrorMessage, EmptyState, LoadingState, Notice, PageHeader } from "@/components/Feedback";
import { api } from "@/lib/api";
import { todayAsDateInput } from "@/lib/format";
import type { AttendanceStatus, AttendanceSummary, Batch, ClassAttendance } from "@/lib/types";

const attendanceStatuses: AttendanceStatus[] = ["PRESENT", "LATE", "ABSENT"];

export default function AttendancePage() {
  const [batches, setBatches] = useState<Batch[]>([]);
  const [batchId, setBatchId] = useState("");
  const [date, setDate] = useState(todayAsDateInput());
  const [classData, setClassData] = useState<ClassAttendance | null>(null);
  const [statuses, setStatuses] = useState<Record<number, AttendanceStatus | "">>({});
  const [remarks, setRemarks] = useState<Record<number, string>>({});
  const [summary, setSummary] = useState<AttendanceSummary | null>(null);
  const [summaryEnrollmentId, setSummaryEnrollmentId] = useState<number | null>(null);
  const [loadingBatches, setLoadingBatches] = useState(true);
  const [loadingClass, setLoadingClass] = useState(false);
  const [loadingSummary, setLoadingSummary] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [success, setSuccess] = useState("");

  useEffect(() => {
    api.getBatches().then(setBatches).catch(setError).finally(() => setLoadingBatches(false));
  }, []);

  const missingStatuses = useMemo(
    () => classData?.records.some((record) => !statuses[record.enrollmentId]) ?? false,
    [classData, statuses],
  );

  async function loadClass(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault();
    if (!batchId || !date) {
      setError(new Error("Choose a batch and attendance date."));
      return;
    }
    setLoadingClass(true);
    setError(null);
    setSuccess("");
    setSummary(null);
    setSummaryEnrollmentId(null);
    try {
      const result = await api.getClassAttendance(Number(batchId), date);
      setClassData(result);
      const nextStatuses: Record<number, AttendanceStatus | ""> = {};
      const nextRemarks: Record<number, string> = {};
      result.records.forEach((record) => {
        nextStatuses[record.enrollmentId] = record.status ?? "";
        nextRemarks[record.enrollmentId] = record.remarks ?? "";
      });
      setStatuses(nextStatuses);
      setRemarks(nextRemarks);
    } catch (loadError) {
      setClassData(null);
      setError(loadError);
    } finally {
      setLoadingClass(false);
    }
  }

  async function submitAttendance(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!classData) return;
    if (missingStatuses) {
      setError(new Error("Choose an attendance status for every student in the class."));
      return;
    }
    setSubmitting(true);
    setError(null);
    setSuccess("");
    try {
      const result = await api.submitClassAttendance({
        batchId: classData.batchId,
        date: classData.date,
        records: classData.records.map((record) => ({
          enrollmentId: record.enrollmentId,
          status: statuses[record.enrollmentId] as AttendanceStatus,
          ...(remarks[record.enrollmentId]?.trim()
            ? { remarks: remarks[record.enrollmentId].trim() }
            : {}),
        })),
      });
      setClassData(result);
      setStatuses(Object.fromEntries(result.records.map((record) => [record.enrollmentId, record.status ?? ""])));
      setRemarks(Object.fromEntries(result.records.map((record) => [record.enrollmentId, record.remarks ?? ""])));
      setSuccess("Class attendance saved. Existing records for this date were updated.");
    } catch (submitError) {
      setError(submitError);
    } finally {
      setSubmitting(false);
    }
  }

  async function showSummary(enrollmentId: number) {
    setLoadingSummary(true);
    setSummaryEnrollmentId(enrollmentId);
    setSummary(null);
    setError(null);
    try {
      setSummary(await api.getAttendanceSummary(enrollmentId));
    } catch (summaryError) {
      setError(summaryError);
    } finally {
      setLoadingSummary(false);
    }
  }

  return (
    <>
      <PageHeader eyebrow="Classroom records" title="Attendance" description="Load an active class roster, mark the whole class, or correct an existing class record." />
      {error && <ApiErrorMessage error={error} />}
      {success && <Notice kind="success">{success}</Notice>}
      <div className="content-stack">
        <section className="card card-pad">
          <div className="section-heading"><div><h2>Choose class and date</h2><p>Every active enrollment in the selected batch is included in the roster.</p></div></div>
          {loadingBatches ? <LoadingState label="Loading batches..." /> : (
            <form className="toolbar" onSubmit={(event) => void loadClass(event)}>
              <div className="field">
                <label htmlFor="attendance-batch">Batch</label>
                <select id="attendance-batch" required value={batchId} onChange={(event) => setBatchId(event.target.value)}>
                  <option value="">Select a batch</option>
                  {batches.map((batch) => <option key={batch.id} value={batch.id}>{batch.code} — {batch.name}</option>)}
                </select>
              </div>
              <div className="field">
                <label htmlFor="attendance-date">Attendance date</label>
                <input id="attendance-date" type="date" required value={date} onChange={(event) => setDate(event.target.value)} />
              </div>
              <button className="button" type="submit" disabled={loadingClass || !batchId}>{loadingClass ? "Loading class..." : "Load class roster"}</button>
            </form>
          )}
        </section>
        {loadingClass && <LoadingState label="Loading the active class roster..." />}
        {classData && !loadingClass && (
          <section className="card card-pad">
            <div className="section-heading">
              <div><h2>{classData.batchCode} · {classData.batchName}</h2><p>Attendance for {classData.date}. Submit the complete roster together.</p></div>
              <span className="badge">{classData.records.length} active student{classData.records.length === 1 ? "" : "s"}</span>
            </div>
            {classData.records.length === 0 ? (
              <EmptyState>No active enrollments were found for this batch.</EmptyState>
            ) : (
              <form onSubmit={(event) => void submitAttendance(event)}>
                <div className="record-list">
                  {classData.records.map((record) => (
                    <article className="record-card" key={record.enrollmentId}>
                      <div className="record-name">
                        <strong>{record.firstName} {record.lastName}</strong>
                        <span>{record.studentNumber} · Enrollment {record.enrollmentId}</span>
                      </div>
                      <div className="field">
                        <label htmlFor={`status-${record.enrollmentId}`}>Attendance status</label>
                        <select id={`status-${record.enrollmentId}`} required value={statuses[record.enrollmentId] ?? ""} onChange={(event) => setStatuses({ ...statuses, [record.enrollmentId]: event.target.value as AttendanceStatus | "" })}>
                          <option value="">Choose status</option>
                          {attendanceStatuses.map((status) => <option key={status} value={status}>{status}</option>)}
                        </select>
                      </div>
                      <div className="record-controls">
                        <div className="field">
                          <label htmlFor={`remarks-${record.enrollmentId}`}>Remarks (optional)</label>
                          <input id={`remarks-${record.enrollmentId}`} maxLength={500} value={remarks[record.enrollmentId] ?? ""} onChange={(event) => setRemarks({ ...remarks, [record.enrollmentId]: event.target.value })} />
                        </div>
                        <div className="field">
                          <span className="field-label">Student summary</span>
                          <button className="button button-secondary button-small" type="button" onClick={() => void showSummary(record.enrollmentId)} disabled={loadingSummary}>
                            {loadingSummary && summaryEnrollmentId === record.enrollmentId ? "Loading..." : "View attendance"}
                          </button>
                        </div>
                      </div>
                    </article>
                  ))}
                </div>
                <div className="form-actions">
                  <button className="button" type="submit" disabled={submitting || missingStatuses}>
                    {submitting ? "Saving class attendance..." : "Save complete class"}
                  </button>
                  {missingStatuses && <span className="field-hint">Select a status for every active student.</span>}
                </div>
              </form>
            )}
          </section>
        )}
        {loadingSummary && <LoadingState label="Loading attendance summary..." />}
        {summary && (
          <section className="card card-pad" aria-live="polite">
            <div className="section-heading"><div><p className="eyebrow">Enrollment {summary.enrollmentId}</p><h2>Attendance summary</h2></div><span className={`badge ${summary.examEligible ? "badge-green" : "badge-red"}`}>{summary.examEligible ? "Exam eligible" : "Not yet eligible"}</span></div>
            <div className="three-column">
              <div className="info-tile"><span>Attendance records</span><strong>{summary.totalRecords}</strong></div>
              <div className="info-tile"><span>Attended (present or late)</span><strong>{summary.attendedCount}</strong></div>
              <div className="info-tile"><span>Attendance percentage</span><strong>{summary.attendancePercentage.toFixed(2)}%</strong></div>
            </div>
          </section>
        )}
      </div>
    </>
  );
}

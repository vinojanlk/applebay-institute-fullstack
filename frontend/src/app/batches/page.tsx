"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { ApiErrorMessage, EmptyState, LoadingState, Notice, PageHeader } from "@/components/Feedback";
import { api } from "@/lib/api";
import type { Batch, BatchRequest, BatchStatus, Course } from "@/lib/types";

const newBatch: BatchRequest = {
  code: "",
  name: "",
  courseId: 0,
  startDate: "",
  endDate: "",
  capacity: 1,
  status: "PLANNED",
};

export default function BatchesPage() {
  const [batches, setBatches] = useState<Batch[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [filterCourseId, setFilterCourseId] = useState("");
  const [form, setForm] = useState<BatchRequest>(newBatch);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [success, setSuccess] = useState("");

  const loadData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [courseRows, batchRows] = await Promise.all([
        api.getCourses(),
        api.getBatches(filterCourseId ? Number(filterCourseId) : undefined),
      ]);
      setCourses(courseRows);
      setBatches(batchRows);
    } catch (loadError) {
      setError(loadError);
    } finally {
      setLoading(false);
    }
  }, [filterCourseId]);

  useEffect(() => {
    let cancelled = false;
    Promise.all([
      api.getCourses(),
      api.getBatches(filterCourseId ? Number(filterCourseId) : undefined),
    ])
      .then(([courseRows, batchRows]) => {
        if (!cancelled) {
          setCourses(courseRows);
          setBatches(batchRows);
          setError(null);
        }
      })
      .catch((loadError) => {
        if (!cancelled) setError(loadError);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [filterCourseId]);

  function resetForm() {
    setForm({ ...newBatch, courseId: courses[0]?.id ?? 0 });
    setEditingId(null);
  }

  function editBatch(batch: Batch) {
    setEditingId(batch.id);
    setForm({
      code: batch.code,
      name: batch.name,
      courseId: batch.courseId,
      startDate: batch.startDate,
      endDate: batch.endDate,
      capacity: batch.capacity,
      status: batch.status,
    });
    setError(null);
    setSuccess("");
  }

  async function submitForm(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (form.endDate <= form.startDate) {
      setError(new Error("The end date must be after the start date."));
      return;
    }
    setSubmitting(true);
    setError(null);
    setSuccess("");
    try {
      if (editingId === null) {
        await api.createBatch(form);
        setSuccess("Batch created successfully.");
      } else {
        await api.updateBatch(editingId, form);
        setSuccess("Batch details updated.");
      }
      resetForm();
      await loadData();
    } catch (submitError) {
      setError(submitError);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Class planning"
        title="Batches"
        description="Organise course groups, dates and class capacity."
      />
      {error && <ApiErrorMessage error={error} />}
      {success && <Notice kind="success">{success}</Notice>}
      <div className="form-layout">
        <section className="card form-card">
          <div className="section-heading">
            <div><h2>{editingId === null ? "Create a batch" : "Edit batch"}</h2><p>Choose the course and planned class dates.</p></div>
          </div>
          <form onSubmit={submitForm}>
            <div className="form-grid form-grid-single">
              <div className="field">
                <label htmlFor="batch-code">Batch code</label>
                <input id="batch-code" required maxLength={30} value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} />
              </div>
              <div className="field">
                <label htmlFor="batch-name">Batch name</label>
                <input id="batch-name" required maxLength={120} value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
              </div>
              <div className="field">
                <label htmlFor="batch-course">Course</label>
                <select id="batch-course" required value={form.courseId || ""} onChange={(event) => setForm({ ...form, courseId: Number(event.target.value) })}>
                  <option value="" disabled>Select a course</option>
                  {courses.map((course) => <option key={course.id} value={course.id}>{course.code} — {course.name}</option>)}
                </select>
              </div>
              <div className="field">
                <label htmlFor="batch-start">Start date</label>
                <input id="batch-start" type="date" required value={form.startDate} onChange={(event) => setForm({ ...form, startDate: event.target.value })} />
              </div>
              <div className="field">
                <label htmlFor="batch-end">End date</label>
                <input id="batch-end" type="date" required min={form.startDate || undefined} value={form.endDate} onChange={(event) => setForm({ ...form, endDate: event.target.value })} />
              </div>
              <div className="field">
                <label htmlFor="batch-capacity">Capacity</label>
                <input id="batch-capacity" type="number" min="1" step="1" required value={form.capacity || ""} onChange={(event) => setForm({ ...form, capacity: Number(event.target.value) })} />
              </div>
              <div className="field">
                <label htmlFor="batch-status">Status</label>
                <select id="batch-status" value={form.status ?? "PLANNED"} onChange={(event) => setForm({ ...form, status: event.target.value as BatchStatus })}>
                  <option value="PLANNED">Planned</option>
                  <option value="IN_PROGRESS">In progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>
            </div>
            <div className="form-actions">
              <button className="button" type="submit" disabled={submitting || courses.length === 0}>
                {submitting ? "Saving..." : editingId === null ? "Create batch" : "Save changes"}
              </button>
              {editingId !== null && <button className="button button-secondary" type="button" onClick={resetForm} disabled={submitting}>Cancel</button>}
            </div>
          </form>
        </section>
        <section className="card table-card">
          <div className="table-card-head">
            <div><h2>Batch list</h2><p>Filter by course or view all batches.</p></div>
            <div className="field">
              <label htmlFor="filter-course">Filter course</label>
              <select id="filter-course" className="control" value={filterCourseId} onChange={(event) => { setLoading(true); setFilterCourseId(event.target.value); }}>
                <option value="">All courses</option>
                {courses.map((course) => <option key={course.id} value={course.id}>{course.code}</option>)}
              </select>
            </div>
          </div>
          {loading ? <LoadingState label="Loading batches..." /> : batches.length === 0 ? (
            <EmptyState>No batches match this selection.</EmptyState>
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead><tr><th>Batch</th><th>Course</th><th>Dates</th><th>Capacity</th><th>Status</th><th>Action</th></tr></thead>
                <tbody>{batches.map((batch) => (
                  <tr key={batch.id}>
                    <td><span className="cell-primary">{batch.code}</span><span className="cell-secondary">{batch.name}</span></td>
                    <td><span className="cell-primary">{batch.courseCode}</span><span className="cell-secondary">{batch.courseName}</span></td>
                    <td>{batch.startDate}<span className="cell-secondary">to {batch.endDate}</span></td>
                    <td>{batch.capacity}</td>
                    <td><span className="badge">{batch.status.replace("_", " ")}</span></td>
                    <td><button className="button button-secondary button-small" type="button" onClick={() => editBatch(batch)}>Edit</button></td>
                  </tr>
                ))}</tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </>
  );
}

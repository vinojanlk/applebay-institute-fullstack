"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { ApiErrorMessage, EmptyState, LoadingState, Notice, PageHeader } from "@/components/Feedback";
import { api } from "@/lib/api";
import { formatMoney } from "@/lib/format";
import type { Course, CourseLevel, CourseRequest } from "@/lib/types";

const emptyForm: CourseRequest = {
  code: "",
  name: "",
  description: "",
  fee: 0,
  level: "HND",
  durationMonths: 1,
};

export default function CoursesPage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [form, setForm] = useState<CourseRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [actionId, setActionId] = useState<number | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [success, setSuccess] = useState("");

  const loadCourses = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setCourses(await api.getCourses());
    } catch (loadError) {
      setError(loadError);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;
    api
      .getCourses()
      .then((rows) => {
        if (!cancelled) {
          setCourses(rows);
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
  }, []);

  function resetForm() {
    setForm(emptyForm);
    setEditingId(null);
  }

  function editCourse(course: Course) {
    setEditingId(course.id);
    setForm({
      code: course.code,
      name: course.name,
      description: course.description ?? "",
      fee: course.fee,
      level: course.level,
      durationMonths: course.durationMonths,
    });
    setSuccess("");
    setError(null);
  }

  async function submitForm(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess("");
    try {
      if (form.fee <= 0 || form.durationMonths <= 0) {
        setError(new Error("Fee and course duration must be greater than zero."));
        return;
      }
      if (editingId === null) {
        await api.createCourse(form);
        setSuccess("Course created successfully.");
      } else {
        await api.updateCourse(editingId, form);
        setSuccess("Course details updated.");
      }
      resetForm();
      await loadCourses();
    } catch (submitError) {
      setError(submitError);
    } finally {
      setSubmitting(false);
    }
  }

  async function deactivateCourse(course: Course) {
    if (!window.confirm(`Deactivate ${course.code}? It will remain in the records.`)) {
      return;
    }
    setActionId(course.id);
    setError(null);
    setSuccess("");
    try {
      await api.deactivateCourse(course.id);
      setSuccess(`${course.code} is now inactive.`);
      await loadCourses();
    } catch (actionError) {
      setError(actionError);
    } finally {
      setActionId(null);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Academic catalogue"
        title="Courses"
        description="Create and maintain the courses offered by the institute."
      />
      {error && <ApiErrorMessage error={error} />}
      {success && <Notice kind="success">{success}</Notice>}
      <div className="form-layout">
        <section className="card form-card">
          <div className="section-heading">
            <div>
              <h2>{editingId === null ? "Add a course" : "Edit course"}</h2>
              <p>Course fees must be greater than zero.</p>
            </div>
          </div>
          <form onSubmit={submitForm}>
            <div className="form-grid form-grid-single">
              <div className="field">
                <label htmlFor="course-code">Course code</label>
                <input id="course-code" required maxLength={30} value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} />
              </div>
              <div className="field">
                <label htmlFor="course-name">Course name</label>
                <input id="course-name" required maxLength={120} value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
              </div>
              <div className="field">
                <label htmlFor="course-description">Description</label>
                <textarea id="course-description" maxLength={2000} value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} />
              </div>
              <div className="field">
                <label htmlFor="course-fee">Total fee (LKR)</label>
                <input id="course-fee" type="number" min="0.01" step="0.01" required value={form.fee || ""} onChange={(event) => setForm({ ...form, fee: Number(event.target.value) })} />
              </div>
              <div className="field">
                <label htmlFor="course-level">Course level</label>
                <select id="course-level" value={form.level} onChange={(event) => setForm({ ...form, level: event.target.value as CourseLevel })}>
                  <option value="HND">HND</option>
                  <option value="CERTIFICATE">Certificate</option>
                </select>
              </div>
              <div className="field">
                <label htmlFor="course-duration">Duration (months)</label>
                <input id="course-duration" type="number" min="1" step="1" required value={form.durationMonths || ""} onChange={(event) => setForm({ ...form, durationMonths: Number(event.target.value) })} />
              </div>
            </div>
            <div className="form-actions">
              <button className="button" type="submit" disabled={submitting}>
                {submitting ? "Saving..." : editingId === null ? "Create course" : "Save changes"}
              </button>
              {editingId !== null && (
                <button className="button button-secondary" type="button" onClick={resetForm} disabled={submitting}>
                  Cancel
                </button>
              )}
            </div>
          </form>
        </section>
        <section className="card table-card">
          <div className="table-card-head">
            <div>
              <h2>Course catalogue</h2>
              <p>{courses.length} course{courses.length === 1 ? "" : "s"} in the institute</p>
            </div>
            <button className="button button-secondary button-small" type="button" onClick={() => void loadCourses()} disabled={loading}>
              Refresh
            </button>
          </div>
          {loading ? (
            <LoadingState label="Loading courses..." />
          ) : courses.length === 0 ? (
            <EmptyState>No courses found. Add the first course using the form.</EmptyState>
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr><th>Course</th><th>Level</th><th>Fee</th><th>Duration</th><th>Status</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {courses.map((course) => (
                    <tr key={course.id}>
                      <td><span className="cell-primary">{course.code}</span><span className="cell-secondary">{course.name}</span></td>
                      <td>{course.level}</td>
                      <td>{formatMoney(course.fee)}</td>
                      <td>{course.durationMonths} months</td>
                      <td><span className={`badge ${course.status === "ACTIVE" ? "badge-green" : "badge-red"}`}>{course.status}</span></td>
                      <td>
                        <div className="table-actions">
                          <button className="button button-secondary button-small" type="button" onClick={() => editCourse(course)}>Edit</button>
                          {course.status === "ACTIVE" && (
                            <button className="button button-danger button-small" type="button" disabled={actionId === course.id} onClick={() => void deactivateCourse(course)}>
                              {actionId === course.id ? "Saving..." : "Deactivate"}
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </>
  );
}

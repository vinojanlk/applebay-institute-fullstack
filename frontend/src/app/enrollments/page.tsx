"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { ApiErrorMessage, EmptyState, LoadingState, Notice, PageHeader } from "@/components/Feedback";
import { api } from "@/lib/api";
import type { Batch, Course, Enrollment, StudentSummary, StudentSearchResponse } from "@/lib/types";

export default function EnrollmentsPage() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [batches, setBatches] = useState<Batch[]>([]);
  const [query, setQuery] = useState("");
  const [studentResults, setStudentResults] = useState<StudentSearchResponse | null>(null);
  const [student, setStudent] = useState<StudentSummary | null>(null);
  const [courseId, setCourseId] = useState("");
  const [batchId, setBatchId] = useState("");
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [loadingEnrollments, setLoadingEnrollments] = useState(false);
  const [searchingStudents, setSearchingStudents] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [success, setSuccess] = useState("");

  useEffect(() => {
    Promise.all([api.getCourses(), api.getBatches()])
      .then(([courseRows, batchRows]) => {
        setCourses(courseRows);
        setBatches(batchRows);
      })
      .catch(setError)
      .finally(() => setLoadingOptions(false));
  }, []);

  const matchingBatches = useMemo(
    () => batches.filter((batch) => String(batch.courseId) === courseId),
    [batches, courseId],
  );

  async function searchStudents(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!query.trim()) {
      setError(new Error("Enter a name, student number, or NIC to find a student."));
      return;
    }
    setSearchingStudents(true);
    setError(null);
    setStudent(null);
    setStudentResults(null);
    try {
      setStudentResults(await api.searchStudents(query.trim(), 0, 10));
    } catch (searchError) {
      setError(searchError);
    } finally {
      setSearchingStudents(false);
    }
  }

  async function chooseStudent(value: StudentSummary) {
    setStudent(value);
    setStudentResults(null);
    setEnrollments([]);
    setLoadingEnrollments(true);
    setError(null);
    try {
      setEnrollments(await api.getStudentEnrollments(value.id));
    } catch (loadError) {
      setError(loadError);
    } finally {
      setLoadingEnrollments(false);
    }
  }

  async function createEnrollment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!student || !courseId || !batchId) {
      setError(new Error("Select a student, course, and batch."));
      return;
    }
    setSubmitting(true);
    setError(null);
    setSuccess("");
    try {
      const created = await api.createEnrollment({
        studentId: student.id,
        courseId: Number(courseId),
        batchId: Number(batchId),
      });
      setSuccess(`${created.studentNumber} is enrolled in ${created.courseCode} — ${created.batchName}.`);
      setEnrollments(await api.getStudentEnrollments(student.id));
      setBatchId("");
    } catch (createError) {
      setError(createError);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Student pathways"
        title="Enrollments"
        description="Choose a student, course and matching batch to create an enrollment."
      />
      {error && <ApiErrorMessage error={error} />}
      {success && <Notice kind="success">{success}</Notice>}
      {loadingOptions ? <LoadingState label="Loading courses and batches..." /> : (
        <div className="content-stack">
          <section className="card card-pad">
            <div className="section-heading">
              <div><h2>Find a student</h2><p>Search by name, student number or NIC.</p></div>
            </div>
            <form className="toolbar" onSubmit={(event) => void searchStudents(event)}>
              <div className="field search-control">
                <label htmlFor="enrollment-student-search">Student search</label>
                <input id="enrollment-student-search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Enter a student search term" />
              </div>
              <button className="button" type="submit" disabled={searchingStudents}>
                {searchingStudents ? "Searching..." : "Search"}
              </button>
            </form>
            {searchingStudents && <LoadingState label="Searching student records..." />}
            {!student && studentResults && (
              studentResults.items.length === 0 ? <EmptyState>No students matched that search.</EmptyState> : (
                <div className="table-wrap">
                  <table className="data-table">
                    <thead><tr><th>Student</th><th>Student number</th><th>NIC</th><th>Choose</th></tr></thead>
                    <tbody>{studentResults.items.map((result) => (
                      <tr key={result.id}>
                        <td>{result.firstName} {result.lastName}</td><td>{result.studentNumber}</td><td>{result.nic}</td>
                        <td><button className="button button-secondary button-small" type="button" onClick={() => void chooseStudent(result)}>Select</button></td>
                      </tr>
                    ))}</tbody>
                  </table>
                </div>
              )
            )}
            {student && (
              <Notice kind="info">
                Selected student: <strong>{student.firstName} {student.lastName}</strong> · {student.studentNumber}
                <button className="button button-secondary button-small" type="button" style={{ marginLeft: 12 }} onClick={() => { setStudent(null); setStudentResults(null); }}>
                  Change
                </button>
              </Notice>
            )}
          </section>
          {student && (
            <div className="two-column">
              <section className="card form-card">
                <div className="section-heading"><div><h2>Create enrollment</h2><p>Batch must belong to the selected course.</p></div></div>
                <form onSubmit={(event) => void createEnrollment(event)}>
                  <div className="form-grid form-grid-single">
                    <div className="field">
                      <label htmlFor="enrollment-course">Course</label>
                      <select id="enrollment-course" required value={courseId} onChange={(event) => { setCourseId(event.target.value); setBatchId(""); }}>
                        <option value="" disabled>Select a course</option>
                        {courses.filter((course) => course.status === "ACTIVE").map((course) => <option key={course.id} value={course.id}>{course.code} — {course.name}</option>)}
                      </select>
                    </div>
                    <div className="field">
                      <label htmlFor="enrollment-batch">Batch</label>
                      <select id="enrollment-batch" required value={batchId} onChange={(event) => setBatchId(event.target.value)} disabled={!courseId}>
                        <option value="" disabled>Select a batch</option>
                        {matchingBatches.map((batch) => <option key={batch.id} value={batch.id}>{batch.code} — {batch.name}</option>)}
                      </select>
                      {courseId && matchingBatches.length === 0 && <span className="field-hint">No batches are listed for this course.</span>}
                    </div>
                  </div>
                  <div className="form-actions"><button className="button" type="submit" disabled={submitting || !courseId || !batchId}>{submitting ? "Creating..." : "Create enrollment"}</button></div>
                </form>
              </section>
              <section className="card table-card">
                <div className="table-card-head"><div><h2>Student enrollments</h2><p>Existing course and batch registrations.</p></div></div>
                {loadingEnrollments ? <LoadingState label="Loading student enrollments..." /> : enrollments.length === 0 ? (
                  <EmptyState>This student has no enrollments yet.</EmptyState>
                ) : (
                  <div className="table-wrap">
                    <table className="data-table">
                      <thead><tr><th>Course</th><th>Batch</th><th>Enrolled</th><th>Status</th></tr></thead>
                      <tbody>{enrollments.map((item) => (
                        <tr key={item.id}><td><span className="cell-primary">{item.courseCode}</span><span className="cell-secondary">{item.courseName}</span></td><td><span className="cell-primary">{item.batchCode}</span><span className="cell-secondary">{item.batchName}</span></td><td>{item.enrolledOn}</td><td><span className="badge badge-green">{item.status}</span></td></tr>
                      ))}</tbody>
                    </table>
                  </div>
                )}
              </section>
            </div>
          )}
        </div>
      )}
    </>
  );
}

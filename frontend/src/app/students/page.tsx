"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { ApiErrorMessage, EmptyState, LoadingState, PageHeader } from "@/components/Feedback";
import { api } from "@/lib/api";
import type { Student, StudentSearchResponse } from "@/lib/types";

const pageSize = 10;

export default function StudentsPage() {
  const [query, setQuery] = useState("");
  const [result, setResult] = useState<StudentSearchResponse | null>(null);
  const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
  const [loading, setLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [detailError, setDetailError] = useState<unknown>(null);

  async function search(page = 0, event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault();
    if (!query.trim()) {
      setError(new Error("Enter a name, student number, or NIC to search."));
      setResult(null);
      return;
    }
    setLoading(true);
    setError(null);
    setSelectedStudent(null);
    try {
      setResult(await api.searchStudents(query.trim(), page, pageSize));
    } catch (searchError) {
      setError(searchError);
    } finally {
      setLoading(false);
    }
  }

  async function showDetails(id: number) {
    setDetailLoading(true);
    setDetailError(null);
    try {
      setSelectedStudent(await api.getStudent(id));
    } catch (loadError) {
      setDetailError(loadError);
      setSelectedStudent(null);
    } finally {
      setDetailLoading(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Student records"
        title="Find a student"
        description="Search by student name, student number or NIC. Results are loaded page by page from the backend."
        actions={<Link className="button" href="/students/new">Register student</Link>}
      />
      {error && <ApiErrorMessage error={error} />}
      <section className="card card-pad">
        <form className="toolbar" onSubmit={(event) => void search(0, event)}>
          <div className="field search-control">
            <label htmlFor="student-query">Student search</label>
            <input id="student-query" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Name, student number, or NIC" />
          </div>
          <button className="button" type="submit" disabled={loading}>{loading ? "Searching..." : "Search students"}</button>
        </form>
        {loading ? <LoadingState label="Searching student records..." /> : result ? (
          result.items.length === 0 ? (
            <EmptyState>No students match “{query}”. Try another search.</EmptyState>
          ) : (
            <>
              <div className="table-wrap">
                <table className="data-table">
                  <thead><tr><th>Student</th><th>Student number</th><th>NIC</th><th>Email</th><th>Registered</th><th>Details</th></tr></thead>
                  <tbody>{result.items.map((student) => (
                    <tr key={student.id}>
                      <td><span className="cell-primary">{student.firstName} {student.lastName}</span></td>
                      <td>{student.studentNumber}</td>
                      <td>{student.nic}</td>
                      <td>{student.email}</td>
                      <td>{new Date(student.createdAt).toLocaleDateString()}</td>
                      <td><button className="button button-secondary button-small" type="button" onClick={() => void showDetails(student.id)} disabled={detailLoading}>View</button></td>
                    </tr>
                  ))}</tbody>
                </table>
              </div>
              <div className="pagination">
                <span>Page {result.totalPages === 0 ? 0 : result.page + 1} of {result.totalPages} · {result.totalElements} student{result.totalElements === 1 ? "" : "s"}</span>
                <div className="pagination-actions">
                  <button className="button button-secondary button-small" type="button" disabled={loading || result.first} onClick={() => void search(result.page - 1)}>Previous</button>
                  <button className="button button-secondary button-small" type="button" disabled={loading || result.last} onClick={() => void search(result.page + 1)}>Next</button>
                </div>
              </div>
            </>
          )
        ) : (
          <EmptyState>Enter a search term to find a student.</EmptyState>
        )}
      </section>
      {detailError && <div className="spacer-top"><ApiErrorMessage error={detailError} /></div>}
      {detailLoading && <LoadingState label="Loading student details..." />}
      {selectedStudent && (
        <section className="card card-pad spacer-top" aria-live="polite">
          <div className="section-heading">
            <div><p className="eyebrow">Student details</p><h2>{selectedStudent.firstName} {selectedStudent.lastName}</h2></div>
            <button className="button button-secondary button-small" type="button" onClick={() => setSelectedStudent(null)}>Close</button>
          </div>
          <div className="three-column">
            <div className="info-tile"><span>Student number</span><strong>{selectedStudent.studentNumber}</strong></div>
            <div className="info-tile"><span>NIC</span><strong>{selectedStudent.nic}</strong></div>
            <div className="info-tile"><span>Email</span><strong>{selectedStudent.email}</strong></div>
            <div className="info-tile"><span>Phone</span><strong>{selectedStudent.phone || "Not provided"}</strong></div>
            <div className="info-tile"><span>Date of birth</span><strong>{selectedStudent.dateOfBirth || "Not provided"}</strong></div>
            <div className="info-tile"><span>Registered</span><strong>{new Date(selectedStudent.createdAt).toLocaleDateString()}</strong></div>
          </div>
          {selectedStudent.address && <p className="subtle-text spacer-top">Address: {selectedStudent.address}</p>}
        </section>
      )}
    </>
  );
}

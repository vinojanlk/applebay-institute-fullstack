"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { ApiErrorMessage, Notice, PageHeader } from "@/components/Feedback";
import { api } from "@/lib/api";
import type { Student, StudentRegistrationRequest } from "@/lib/types";

const initialForm: StudentRegistrationRequest = {
  firstName: "",
  lastName: "",
  nic: "",
  email: "",
  phone: "",
  address: "",
  dateOfBirth: "",
};

export default function NewStudentPage() {
  const [form, setForm] = useState(initialForm);
  const [createdStudent, setCreatedStudent] = useState<Student | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<unknown>(null);

  async function register(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setCreatedStudent(null);
    try {
      const request: StudentRegistrationRequest = {
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        nic: form.nic.trim(),
        email: form.email.trim().toLowerCase(),
        phone: form.phone.trim(),
        address: form.address.trim(),
        ...(form.dateOfBirth ? { dateOfBirth: form.dateOfBirth } : {}),
      };
      setCreatedStudent(await api.registerStudent(request));
      setForm(initialForm);
    } catch (registrationError) {
      setError(registrationError);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Student records"
        title="Register a student"
        description="The student number is generated automatically when the record is saved."
        actions={<Link className="button button-secondary" href="/students">Find a student</Link>}
      />
      {error && <ApiErrorMessage error={error} />}
      {createdStudent && (
        <Notice kind="success">
          <strong>Student registered.</strong> Student number: {createdStudent.studentNumber}
        </Notice>
      )}
      <section className="card form-card">
        <form onSubmit={(event) => void register(event)}>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="first-name">First name</label>
              <input id="first-name" autoComplete="given-name" required maxLength={80} value={form.firstName} onChange={(event) => setForm({ ...form, firstName: event.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="last-name">Last name</label>
              <input id="last-name" autoComplete="family-name" required maxLength={80} value={form.lastName} onChange={(event) => setForm({ ...form, lastName: event.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="student-nic">NIC</label>
              <input id="student-nic" required maxLength={20} value={form.nic} onChange={(event) => setForm({ ...form, nic: event.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="student-email">Email address</label>
              <input id="student-email" type="email" autoComplete="email" required maxLength={254} value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="student-phone">Phone</label>
              <input id="student-phone" type="tel" autoComplete="tel" maxLength={25} value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} />
            </div>
            <div className="field">
              <label htmlFor="student-date-of-birth">Date of birth</label>
              <input id="student-date-of-birth" type="date" value={form.dateOfBirth} onChange={(event) => setForm({ ...form, dateOfBirth: event.target.value })} />
            </div>
            <div className="field field-full">
              <label htmlFor="student-address">Address</label>
              <textarea id="student-address" autoComplete="street-address" maxLength={500} value={form.address} onChange={(event) => setForm({ ...form, address: event.target.value })} />
            </div>
          </div>
          <p className="field-hint spacer-top">Student number and registration date are assigned by the backend.</p>
          <div className="form-actions">
            <button className="button" type="submit" disabled={submitting}>{submitting ? "Registering..." : "Register student"}</button>
          </div>
        </form>
      </section>
    </>
  );
}

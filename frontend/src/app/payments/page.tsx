"use client";

import { FormEvent, useState } from "react";
import { ApiErrorMessage, EmptyState, LoadingState, Notice, PageHeader } from "@/components/Feedback";
import { api } from "@/lib/api";
import { formatMoney, todayAsDateInput } from "@/lib/format";
import type { Enrollment, FeeSummary, Payment, PaymentMethod, StudentSearchResponse, StudentSummary } from "@/lib/types";

export default function PaymentsPage() {
  const [query, setQuery] = useState("");
  const [students, setStudents] = useState<StudentSearchResponse | null>(null);
  const [student, setStudent] = useState<StudentSummary | null>(null);
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [enrollmentId, setEnrollmentId] = useState("");
  const [summary, setSummary] = useState<FeeSummary | null>(null);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [amount, setAmount] = useState("");
  const [paymentDate, setPaymentDate] = useState(todayAsDateInput());
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>("CASH");
  const [reference, setReference] = useState("");
  const [loading, setLoading] = useState(false);
  const [searchingStudents, setSearchingStudents] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [success, setSuccess] = useState("");

  async function searchStudents(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!query.trim()) {
      setError(new Error("Enter a name, student number, or NIC to find a student."));
      return;
    }
    setError(null);
    setStudent(null);
    setEnrollments([]);
    setEnrollmentId("");
    setSummary(null);
    setPayments([]);
    setSearchingStudents(true);
    try {
      setStudents(await api.searchStudents(query.trim(), 0, 10));
    } catch (searchError) {
      setError(searchError);
    } finally {
      setSearchingStudents(false);
    }
  }

  async function selectStudent(value: StudentSummary) {
    setStudent(value);
    setStudents(null);
    setEnrollmentId("");
    setSummary(null);
    setPayments([]);
    setLoading(true);
    setError(null);
    try {
      setEnrollments(await api.getStudentEnrollments(value.id));
    } catch (loadError) {
      setError(loadError);
    } finally {
      setLoading(false);
    }
  }

  async function selectEnrollment(value: string) {
    setEnrollmentId(value);
    setSummary(null);
    setPayments([]);
    if (!value) return;
    setLoading(true);
    setError(null);
    try {
      const [feeSummary, history] = await Promise.all([
        api.getFeeSummary(Number(value)),
        api.getPayments(Number(value)),
      ]);
      setSummary(feeSummary);
      setPayments(history);
    } catch (loadError) {
      setError(loadError);
    } finally {
      setLoading(false);
    }
  }

  async function recordPayment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!enrollmentId || Number(amount) <= 0) {
      setError(new Error("Select an enrollment and enter an amount greater than zero."));
      return;
    }
    setSubmitting(true);
    setError(null);
    setSuccess("");
    try {
      const created = await api.createPayment({
        enrollmentId: Number(enrollmentId),
        amount: Number(amount),
        paymentDate,
        paymentMethod,
        ...(reference.trim() ? { reference: reference.trim() } : {}),
      });
      setSuccess(`Payment recorded. Receipt number: ${created.receiptNumber}.`);
      setAmount("");
      setReference("");
      const [feeSummary, history] = await Promise.all([
        api.getFeeSummary(Number(enrollmentId)),
        api.getPayments(Number(enrollmentId)),
      ]);
      setSummary(feeSummary);
      setPayments(history);
    } catch (paymentError) {
      setError(paymentError);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <PageHeader eyebrow="Finance desk" title="Payments" description="Record instalments and review the fees for a student enrollment." />
      {error && <ApiErrorMessage error={error} />}
      {success && <Notice kind="success">{success}</Notice>}
      <div className="content-stack">
        <section className="card card-pad">
          <div className="section-heading"><div><h2>Choose an enrollment</h2><p>Search for a student first, then select one of their enrollments.</p></div></div>
          <form className="toolbar" onSubmit={(event) => void searchStudents(event)}>
            <div className="field search-control"><label htmlFor="payment-student-search">Student search</label><input id="payment-student-search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Name, student number, or NIC" /></div>
            <button className="button" type="submit" disabled={searchingStudents}>
              {searchingStudents ? "Searching..." : "Search"}
            </button>
          </form>
          {searchingStudents && <LoadingState label="Searching student records..." />}
          {students && (
            students.items.length === 0 ? <EmptyState>No students matched that search.</EmptyState> : (
              <div className="table-wrap"><table className="data-table">
                <thead><tr><th>Student</th><th>Student number</th><th>NIC</th><th>Choose</th></tr></thead>
                <tbody>{students.items.map((row) => <tr key={row.id}><td>{row.firstName} {row.lastName}</td><td>{row.studentNumber}</td><td>{row.nic}</td><td><button className="button button-secondary button-small" type="button" onClick={() => void selectStudent(row)}>Select</button></td></tr>)}</tbody>
              </table></div>
            )
          )}
          {student && (
            <div className="form-grid spacer-top">
              <div className="field"><label htmlFor="selected-student">Selected student</label><input id="selected-student" readOnly value={`${student.firstName} ${student.lastName} · ${student.studentNumber}`} /></div>
              <div className="field"><label htmlFor="payment-enrollment">Enrollment</label>
                <select id="payment-enrollment" value={enrollmentId} onChange={(event) => void selectEnrollment(event.target.value)} disabled={loading || enrollments.length === 0}>
                  <option value="">Select an enrollment</option>
                  {enrollments.map((item) => <option key={item.id} value={item.id}>{item.courseCode} · {item.batchCode} · {item.status}</option>)}
                </select>
              </div>
            </div>
          )}
          {student && !loading && enrollments.length === 0 && <div className="spacer-top"><EmptyState>This student has no enrollments to receive a payment.</EmptyState></div>}
        </section>
        {loading && <LoadingState label="Loading enrollment fee details..." />}
        {summary && (
          <div className="two-column">
            <section className="card card-pad">
              <div className="section-heading"><div><h2>Fee summary</h2><p>Calculated by the backend from the selected enrollment.</p></div></div>
              <div className="three-column">
                <div className="info-tile"><span>Course fee</span><strong>{formatMoney(summary.courseFee)}</strong></div>
                <div className="info-tile"><span>Total paid</span><strong>{formatMoney(summary.totalPaid)}</strong></div>
                <div className="info-tile"><span>Outstanding</span><strong>{formatMoney(summary.outstandingBalance)}</strong></div>
              </div>
            </section>
            <section className="card form-card">
              <div className="section-heading"><div><h2>Record a payment</h2><p>Receipt number is generated after saving.</p></div></div>
              <form onSubmit={(event) => void recordPayment(event)}>
                <div className="form-grid">
                  <div className="field"><label htmlFor="payment-amount">Amount (LKR)</label><input id="payment-amount" type="number" min="0.01" step="0.01" required value={amount} onChange={(event) => setAmount(event.target.value)} /></div>
                  <div className="field"><label htmlFor="payment-date">Payment date</label><input id="payment-date" type="date" required value={paymentDate} onChange={(event) => setPaymentDate(event.target.value)} /></div>
                  <div className="field"><label htmlFor="payment-method">Payment method</label><select id="payment-method" value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value as PaymentMethod)}><option value="CASH">Cash</option><option value="BANK_TRANSFER">Bank transfer</option><option value="CARD">Card</option><option value="ONLINE">Online</option></select></div>
                  <div className="field"><label htmlFor="payment-reference">Reference (optional)</label><input id="payment-reference" maxLength={100} value={reference} onChange={(event) => setReference(event.target.value)} /></div>
                </div>
                <div className="form-actions"><button className="button" type="submit" disabled={submitting || !enrollmentId}>{submitting ? "Saving payment..." : "Record payment"}</button></div>
              </form>
            </section>
          </div>
        )}
        {enrollmentId && !loading && (
          <section className="card table-card">
            <div className="table-card-head"><div><h2>Payment history</h2><p>Receipts recorded for this enrollment.</p></div></div>
            {payments.length === 0 ? <EmptyState>No payments have been recorded for this enrollment.</EmptyState> : (
              <div className="table-wrap"><table className="data-table">
                <thead><tr><th>Receipt</th><th>Date</th><th>Method</th><th>Amount</th><th>Reference</th></tr></thead>
                <tbody>{payments.map((payment) => <tr key={payment.id}><td><span className="cell-primary">{payment.receiptNumber}</span></td><td>{payment.paymentDate}</td><td>{payment.paymentMethod.replace("_", " ")}</td><td>{formatMoney(payment.amount)}</td><td>{payment.reference || "—"}</td></tr>)}</tbody>
              </table></div>
            )}
          </section>
        )}
      </div>
    </>
  );
}

"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { ApiErrorMessage, LoadingState, Notice, PageHeader } from "@/components/Feedback";
import { api } from "@/lib/api";
import { formatMoney } from "@/lib/format";
import type { DashboardSummary } from "@/lib/types";

const quickLinks = [
  { href: "/students/new", label: "Register a student" },
  { href: "/attendance", label: "Take attendance" },
  { href: "/payments", label: "Record a payment" },
  { href: "/courses", label: "Manage courses" },
];

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);

  const loadSummary = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setSummary(await api.getDashboard());
    } catch (loadError) {
      setError(loadError);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadSummary();
  }, [loadSummary]);

  return (
    <>
      <PageHeader
        eyebrow="Institute overview"
        title="Good day. Here is your overview."
        description="A quick view of students, courses, attendance and fees at AppleBay Institute."
      />
      {error && (
        <>
          <ApiErrorMessage error={error} />
          <button className="button button-secondary" type="button" onClick={() => void loadSummary()} disabled={loading}>
            Retry
          </button>
        </>
      )}
      {loading && <LoadingState label="Loading the latest institute summary..." />}
      {!loading && !error && summary && (
        <>
          <section className="dashboard-grid" aria-label="Institute summary">
            <article className="card stat-card">
              <span className="stat-label">Total students</span>
              <strong className="stat-value">{summary.totalStudents}</strong>
              <span className="stat-note">Registered student records</span>
            </article>
            <article className="card stat-card">
              <span className="stat-label">Active courses</span>
              <strong className="stat-value">{summary.activeCourses}</strong>
              <span className="stat-note">Currently available courses</span>
            </article>
            <article className="card stat-card">
              <span className="stat-label">Present today</span>
              <strong className="stat-value">{summary.todayPresentCount}</strong>
              <span className="stat-note">Present or late attendance today</span>
            </article>
            <article className="card stat-card">
              <span className="stat-label">Fees collected</span>
              <strong className="stat-value stat-money">
                {formatMoney(summary.totalFeesCollected)}
              </strong>
              <span className="stat-note">Total payments recorded</span>
            </article>
          </section>
          <section className="card dashboard-welcome">
            <div>
              <p className="eyebrow">Daily workspace</p>
              <h2>Keep the institute moving.</h2>
              <p>
                Use the shortcuts to work with student records, classroom
                attendance and course payments. All information is loaded from
                the institute backend.
              </p>
            </div>
            <div className="quick-links">
              {quickLinks.map((item) => (
                <Link href={item.href} className="quick-link" key={item.href}>
                  {item.label}
                  <span aria-hidden="true">→</span>
                </Link>
              ))}
            </div>
          </section>
        </>
      )}
      {!loading && !error && !summary && (
        <Notice kind="info">No dashboard summary was returned.</Notice>
      )}
    </>
  );
}

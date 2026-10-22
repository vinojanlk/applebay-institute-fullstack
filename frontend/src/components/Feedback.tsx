import type { ReactNode } from "react";
import { ApiError } from "@/lib/api";

export function PageHeader({
  eyebrow,
  title,
  description,
  actions,
}: {
  eyebrow?: string;
  title: string;
  description?: string;
  actions?: ReactNode;
}) {
  return (
    <div className="page-header">
      <div>
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <h1>{title}</h1>
        {description && <p className="page-description">{description}</p>}
      </div>
      {actions && <div className="page-header-actions">{actions}</div>}
    </div>
  );
}

export function Notice({
  kind,
  children,
}: {
  kind: "success" | "error" | "info";
  children: ReactNode;
}) {
  return (
    <div className={`notice notice-${kind}`} role={kind === "error" ? "alert" : "status"}>
      {children}
    </div>
  );
}

export function ApiErrorMessage({ error }: { error: unknown }) {
  if (error instanceof Error && !(error instanceof ApiError)) {
    return <Notice kind="error">{error.message}</Notice>;
  }

  if (!(error instanceof ApiError)) {
    return <Notice kind="error">An unexpected error occurred. Please try again.</Notice>;
  }

  return (
    <Notice kind="error">
      <p>{error.message}</p>
      {Object.keys(error.fieldErrors).length > 0 && (
        <ul className="field-error-list">
          {Object.entries(error.fieldErrors).map(([field, message]) => (
            <li key={field}>
              <strong>{field}:</strong> {message}
            </li>
          ))}
        </ul>
      )}
    </Notice>
  );
}

export function LoadingState({ label = "Loading..." }: { label?: string }) {
  return (
    <div className="loading-state" role="status">
      <span className="loading-dot" aria-hidden="true" />
      {label}
    </div>
  );
}

export function EmptyState({ children }: { children: ReactNode }) {
  return <div className="empty-state">{children}</div>;
}

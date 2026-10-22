import type {
  AttendanceSummary,
  Batch,
  BatchRequest,
  ClassAttendance,
  ClassAttendanceRequest,
  Course,
  CourseRequest,
  DashboardSummary,
  Enrollment,
  EnrollmentRequest,
  FeeSummary,
  Payment,
  PaymentRequest,
  Student,
  StudentRegistrationRequest,
  StudentSearchResponse,
} from "@/lib/types";

const apiBaseUrl = (() => {
  const value = process.env.NEXT_PUBLIC_API_BASE_URL;
  if (!value) {
    throw new Error(
      "NEXT_PUBLIC_API_BASE_URL is missing. Set it in frontend/.env.local and restart Next.js.",
    );
  }
  return value;
})();

export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors: Record<string, string>;

  constructor(
    message: string,
    status: number,
    fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

async function request<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");

  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  let response: Response;

  try {
    response = await fetch(`${apiBaseUrl.replace(/\/+$/, "")}${path}`, {
      ...options,
      headers,
      cache: "no-store",
    });
  } catch {
    throw new ApiError(
      "Could not reach the backend. Check that it is running and try again.",
      0,
    );
  }

  const responseText = await response.text();
  let responseBody: unknown;

  if (responseText.trim()) {
    try {
      responseBody = JSON.parse(responseText) as unknown;
    } catch {
      if (response.ok) {
        throw new ApiError(
          "The backend returned an unreadable response.",
          response.status,
        );
      }
    }
  }

  if (!response.ok) {
    const errorBody =
      responseBody && typeof responseBody === "object"
        ? (responseBody as {
            message?: unknown;
            fieldErrors?: unknown;
          })
        : {};
    const fieldErrors =
      errorBody.fieldErrors &&
      typeof errorBody.fieldErrors === "object" &&
      !Array.isArray(errorBody.fieldErrors)
        ? (errorBody.fieldErrors as Record<string, string>)
        : {};
    const message =
      typeof errorBody.message === "string"
        ? errorBody.message
        : `The request failed with HTTP ${response.status}.`;

    throw new ApiError(message, response.status, fieldErrors);
  }

  if (response.status === 204 || !responseText.trim()) {
    return undefined as T;
  }

  return responseBody as T;
}

function get<T>(path: string): Promise<T> {
  return request<T>(path);
}

function post<T>(path: string, body: unknown): Promise<T> {
  return request<T>(path, { method: "POST", body: JSON.stringify(body) });
}

function put<T>(path: string, body: unknown): Promise<T> {
  return request<T>(path, { method: "PUT", body: JSON.stringify(body) });
}

function patch<T>(path: string): Promise<T> {
  return request<T>(path, { method: "PATCH" });
}

export const api = {
  getDashboard: () => get<DashboardSummary>("/api/dashboard/summary"),
  getCourses: () => get<Course[]>("/api/courses"),
  getCourse: (id: number) => get<Course>(`/api/courses/${id}`),
  createCourse: (body: CourseRequest) =>
    post<Course>("/api/courses", body),
  updateCourse: (id: number, body: CourseRequest) =>
    put<Course>(`/api/courses/${id}`, body),
  deactivateCourse: (id: number) =>
    patch<Course>(`/api/courses/${id}/deactivate`),
  getBatches: (courseId?: number) =>
    get<Batch[]>(
      courseId ? `/api/batches?courseId=${courseId}` : "/api/batches",
    ),
  getBatch: (id: number) => get<Batch>(`/api/batches/${id}`),
  createBatch: (body: BatchRequest) => post<Batch>("/api/batches", body),
  updateBatch: (id: number, body: BatchRequest) =>
    put<Batch>(`/api/batches/${id}`, body),
  registerStudent: (body: StudentRegistrationRequest) =>
    post<Student>("/api/students", body),
  getStudent: (id: number) => get<Student>(`/api/students/${id}`),
  searchStudents: (query: string, page: number, size: number) =>
    get<StudentSearchResponse>(
      `/api/students/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`,
    ),
  createEnrollment: (body: EnrollmentRequest) =>
    post<Enrollment>("/api/enrollments", body),
  getStudentEnrollments: (studentId: number) =>
    get<Enrollment[]>(`/api/students/${studentId}/enrollments`),
  createPayment: (body: PaymentRequest) =>
    post<Payment>("/api/payments", body),
  getPayments: (enrollmentId: number) =>
    get<Payment[]>(`/api/enrollments/${enrollmentId}/payments`),
  getFeeSummary: (enrollmentId: number) =>
    get<FeeSummary>(`/api/enrollments/${enrollmentId}/fee-summary`),
  getClassAttendance: (batchId: number, date: string) =>
    get<ClassAttendance>(
      `/api/attendance/class?batchId=${batchId}&date=${encodeURIComponent(date)}`,
    ),
  submitClassAttendance: (body: ClassAttendanceRequest) =>
    post<ClassAttendance>("/api/attendance/class", body),
  getAttendanceSummary: (enrollmentId: number) =>
    get<AttendanceSummary>(
      `/api/attendance/student/${enrollmentId}/summary`,
    ),
};

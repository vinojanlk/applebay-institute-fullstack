export type CourseLevel = "HND" | "CERTIFICATE";
export type CourseStatus = "ACTIVE" | "INACTIVE";
export type BatchStatus = "PLANNED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
export type AttendanceStatus = "PRESENT" | "LATE" | "ABSENT";
export type PaymentMethod = "CASH" | "BANK_TRANSFER" | "CARD" | "ONLINE";

export interface Course {
  id: number;
  code: string;
  name: string;
  description: string | null;
  fee: number;
  level: CourseLevel;
  durationMonths: number;
  status: CourseStatus;
}

export interface CourseRequest {
  code: string;
  name: string;
  description: string;
  fee: number;
  level: CourseLevel;
  durationMonths: number;
}

export interface Batch {
  id: number;
  code: string;
  name: string;
  courseId: number;
  courseCode: string;
  courseName: string;
  startDate: string;
  endDate: string;
  capacity: number;
  status: BatchStatus;
}

export interface BatchRequest {
  code: string;
  name: string;
  courseId: number;
  startDate: string;
  endDate: string;
  capacity: number;
  status?: BatchStatus;
}

export interface Student {
  id: number;
  firstName: string;
  lastName: string;
  studentNumber: string;
  nic: string;
  email: string;
  phone: string | null;
  address: string | null;
  dateOfBirth: string | null;
  createdAt: string;
}

export interface StudentSummary {
  id: number;
  firstName: string;
  lastName: string;
  studentNumber: string;
  nic: string;
  email: string;
  createdAt: string;
}

export interface StudentSearchResponse {
  items: StudentSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface StudentRegistrationRequest {
  firstName: string;
  lastName: string;
  nic: string;
  email: string;
  phone: string;
  address: string;
  dateOfBirth?: string;
}

export interface Enrollment {
  id: number;
  studentId: number;
  studentNumber: string;
  firstName: string;
  lastName: string;
  courseId: number;
  courseCode: string;
  courseName: string;
  batchId: number;
  batchCode: string;
  batchName: string;
  enrolledOn: string;
  status: "ACTIVE" | "COMPLETED" | "WITHDRAWN";
}

export interface EnrollmentRequest {
  studentId: number;
  courseId: number;
  batchId: number;
}

export interface Payment {
  id: number;
  enrollmentId: number;
  receiptNumber: string;
  amount: number;
  paymentDate: string;
  paymentMethod: PaymentMethod;
  reference: string | null;
}

export interface PaymentRequest {
  enrollmentId: number;
  amount: number;
  paymentDate: string;
  paymentMethod: PaymentMethod;
  reference?: string;
}

export interface FeeSummary {
  enrollmentId: number;
  courseFee: number;
  totalPaid: number;
  outstandingBalance: number;
}

export interface AttendanceRecord {
  enrollmentId: number;
  studentId: number;
  studentNumber: string;
  firstName: string;
  lastName: string;
  status: AttendanceStatus | null;
  remarks: string | null;
}

export interface ClassAttendance {
  batchId: number;
  batchCode: string;
  batchName: string;
  date: string;
  records: AttendanceRecord[];
}

export interface AttendanceRecordRequest {
  enrollmentId: number;
  status: AttendanceStatus;
  remarks?: string;
}

export interface ClassAttendanceRequest {
  batchId: number;
  date: string;
  records: AttendanceRecordRequest[];
}

export interface AttendanceSummary {
  enrollmentId: number;
  totalRecords: number;
  attendedCount: number;
  attendancePercentage: number;
  examEligible: boolean;
}

export interface DashboardSummary {
  totalStudents: number;
  activeCourses: number;
  todayPresentCount: number;
  totalFeesCollected: number;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: Record<string, string>;
}

export interface ApiError {
  message: string;
  status?: number;
  fieldErrors?: Record<string, string>;
}
# Applebay Full-Stack Project Instructions

This is an individual university assignment developed using:

- Next.js App Router with JavaScript
- Spring Boot with Java 21 and Maven
- Spring Data JPA and Bean Validation
- PostgreSQL
- REST API with JSON

## Working method

Work in small phases.

Before changing files:
1. Inspect the workspace.
2. Explain the plan.
3. Do not delete existing files automatically.

After each phase:
1. Compile or test the code.
2. Report the commands and results honestly.
3. Explain the changed files in simple English.
4. Give a short viva checklist.
5. Stop and wait for approval.

Never build the complete application in one step.

## Required folders

The project root must contain:

- frontend/
- backend/
- database/
- document/

Do not include node_modules, .next or target in the final submission.

## Backend rules

Use these packages:

- controller
- service
- repository
- entity
- dto
- dto.request
- dto.response
- exception
- config

Controllers must only receive requests, validate DTOs, call services and return responses.

All business rules must remain in services.

Repositories must handle database operations only.

Do not return JPA entities directly from controllers. Use separate request and response DTOs.

Use a global exception handler with consistent JSON errors.

Use @Transactional where several related database changes must succeed or fail together.

## Project rules

The system manages:

- courses
- batches
- students
- enrollments
- attendance
- payments
- dashboard summaries

Important business rules:

- Course code must be unique.
- Student number must be generated automatically.
- NIC and email must be unique.
- Duplicate enrollment must be prevented.
- Attendance must be submitted as one class request.
- Existing attendance must be corrected instead of duplicated.
- PRESENT and LATE count as attendance.
- Exam eligibility requires at least 80% attendance.
- Receipt numbers must be generated automatically.
- Outstanding balance must be calculated from payments.
- Overpayments must be rejected.
- Student searching must use server-side pagination.

## Simplicity

Use understandable student-level code.

Do not add:

- authentication
- JWT
- Docker
- microservices
- Redux
- Lombok
- MapStruct
- complicated UI libraries
- advanced features outside the assignment

Use meaningful names and short comments only where logic is difficult.

Never create fake screenshots, fake test results or invented execution results.
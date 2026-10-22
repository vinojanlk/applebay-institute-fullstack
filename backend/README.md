# AppleBay Backend

This is the Spring Boot backend for the AppleBay institute management system.
It uses Java 21, Maven, Spring Data JPA, Bean Validation, and PostgreSQL.

## Requirements

- Java 21
- PostgreSQL

The Maven Wrapper is included, so a separate Maven installation is not
required. Java 21 is still required to run the backend. The first wrapper run
may need internet access to download the configured Maven distribution.

## Database setup

Create a PostgreSQL database named `applebay`. To create the initial tables
manually, run `database/schema.sql` from the project root using your PostgreSQL
client. To add the small starter course and batch records, run
`database/seed-basic.sql` after the schema.

The backend connects to `jdbc:postgresql://localhost:5432/applebay` by default
and uses `applebay_app` as the default database username. Configure these
environment variables as needed:

- `DB_URL`: PostgreSQL JDBC URL (default: `jdbc:postgresql://localhost:5432/applebay`)
- `DB_USERNAME`: database username (default: `applebay_app`)
- `DB_PASSWORD`: database password; you must set this locally before starting
  the backend. For example, use a private value such as
  `<your-private-password>` in your local environment, not in this file.

Never commit passwords or other secrets to Git. Hibernate validates the
existing database schema at startup and does not create, update, or delete
tables automatically. SQL scripts are not run automatically.

## Build

From the `backend` directory, run the tests with the wrapper:

```text
.\mvnw.cmd test
```

To start the backend from the `backend` directory, first set `DB_PASSWORD` in
your local environment, then run:

```text
.\mvnw.cmd spring-boot:run
```

## Initial model

The initial entities are `Course`, `Batch`, `Student`, `Enrollment`,
`Attendance`, and `Payment`. Their enum values are stored as readable strings
in PostgreSQL.

INSERT INTO courses (code, name, description, fee, level, duration_months, status)
VALUES
    ('WEB-101', 'Web Development Basics', 'Introduction to building websites.', 450.00, 'HND', 3, 'ACTIVE'),
    ('JAVA-101', 'Java Programming Basics', 'Introduction to Java programming.', 500.00, 'CERTIFICATE', 4, 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO batches (code, name, course_id, start_date, end_date, capacity, status)
SELECT 'WEB-101-2026-01', 'Web Development January 2026', id,
       DATE '2026-01-15', DATE '2026-04-09', 25, 'PLANNED'
FROM courses
WHERE code = 'WEB-101'
ON CONFLICT (code) DO NOTHING;

INSERT INTO batches (code, name, course_id, start_date, end_date, capacity, status)
SELECT 'JAVA-101-2026-01', 'Java Programming January 2026', id,
       DATE '2026-01-20', DATE '2026-04-28', 25, 'PLANNED'
FROM courses
WHERE code = 'JAVA-101'
ON CONFLICT (code) DO NOTHING;

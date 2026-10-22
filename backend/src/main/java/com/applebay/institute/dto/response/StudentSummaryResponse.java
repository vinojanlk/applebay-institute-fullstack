package com.applebay.institute.dto.response;

import java.time.Instant;

public class StudentSummaryResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String studentNumber;
    private final String nic;
    private final String email;
    private final Instant createdAt;

    public StudentSummaryResponse(
            Long id,
            String firstName,
            String lastName,
            String studentNumber,
            String nic,
            String email,
            Instant createdAt
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentNumber = studentNumber;
        this.nic = nic;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getNic() {
        return nic;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

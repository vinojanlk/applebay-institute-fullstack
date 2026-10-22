package com.applebay.institute.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public class StudentResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String studentNumber;
    private final String nic;
    private final String email;
    private final String phone;
    private final String address;
    private final LocalDate dateOfBirth;
    private final Instant createdAt;

    public StudentResponse(
            Long id,
            String firstName,
            String lastName,
            String studentNumber,
            String nic,
            String email,
            String phone,
            String address,
            LocalDate dateOfBirth,
            Instant createdAt
    ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentNumber = studentNumber;
        this.nic = nic;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
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

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

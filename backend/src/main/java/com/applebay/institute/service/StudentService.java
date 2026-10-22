package com.applebay.institute.service;

import com.applebay.institute.dto.request.StudentRegistrationRequest;
import com.applebay.institute.dto.response.StudentResponse;
import com.applebay.institute.dto.response.StudentSearchResponse;
import com.applebay.institute.dto.response.StudentSummaryResponse;
import com.applebay.institute.entity.Student;
import com.applebay.institute.exception.DuplicateResourceException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Locale;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StudentResponse registerStudent(StudentRegistrationRequest request) {
        String nic = request.getNic().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (studentRepository.existsByNic(nic)) {
            throw new DuplicateResourceException("A student with this NIC already exists");
        }
        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A student with this email already exists");
        }

        Student student = new Student();
        student.setFirstName(request.getFirstName().trim());
        student.setLastName(request.getLastName().trim());
        student.setNic(nic);
        student.setEmail(email);
        student.setPhone(trimToNull(request.getPhone()));
        student.setAddress(trimToNull(request.getAddress()));
        student.setDateOfBirth(request.getDateOfBirth());
        student.setStudentNumber(generateStudentNumber());
        student.setCreatedAt(Instant.now());
        return toResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student " + id + " was not found"));
        return toResponse(student);
    }

    @Transactional(readOnly = true)
    public StudentSearchResponse searchStudents(String query, int page, int size) {
        String trimmedQuery = query.trim();
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by("lastName").ascending().and(Sort.by("firstName").ascending())
        );
        Page<Student> resultPage = studentRepository.searchStudents(trimmedQuery, pageRequest);
        List<StudentSummaryResponse> students = resultPage.getContent().stream()
                .map(StudentService::toSummaryResponse)
                .toList();

        return new StudentSearchResponse(
                students,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
    }

    private String generateStudentNumber() {
        Long sequenceValue = studentRepository.nextStudentNumberValue();
        return "AB" + Year.now().getValue() + "-"
                + String.format(Locale.ROOT, "%04d", sequenceValue);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private static StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getStudentNumber(),
                student.getNic(),
                student.getEmail(),
                student.getPhone(),
                student.getAddress(),
                student.getDateOfBirth(),
                student.getCreatedAt()
        );
    }

    private static StudentSummaryResponse toSummaryResponse(Student student) {
        return new StudentSummaryResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getStudentNumber(),
                student.getNic(),
                student.getEmail(),
                student.getCreatedAt()
        );
    }
}

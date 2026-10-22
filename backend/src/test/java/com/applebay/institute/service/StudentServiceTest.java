package com.applebay.institute.service;

import com.applebay.institute.dto.request.StudentRegistrationRequest;
import com.applebay.institute.dto.response.StudentResponse;
import com.applebay.institute.dto.response.StudentSearchResponse;
import com.applebay.institute.entity.Student;
import com.applebay.institute.exception.DuplicateResourceException;
import com.applebay.institute.exception.ResourceNotFoundException;
import com.applebay.institute.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void registrationGeneratesStudentNumberAndNormalizesInput() {
        StudentRegistrationRequest request = registrationRequest();
        when(studentRepository.existsByNic("123456789")).thenReturn(false);
        when(studentRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(studentRepository.nextStudentNumberValue()).thenReturn(1L);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student student = invocation.getArgument(0);
            student.setId(10L);
            return student;
        });

        StudentResponse response = studentService.registerStudent(request);

        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("123456789", response.getNic());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals("0771234567", response.getPhone());
        assertEquals("Main Street", response.getAddress());
        assertTrue(Pattern.matches(
                "AB" + Year.now().getValue() + "-0001",
                response.getStudentNumber()
        ));
        assertNotNull(response.getCreatedAt());
        verify(studentRepository).nextStudentNumberValue();
    }

    @Test
    void duplicateNicIsRejected() {
        StudentRegistrationRequest request = registrationRequest();
        when(studentRepository.existsByNic("123456789")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.registerStudent(request));
        verify(studentRepository, never()).save(any(Student.class));
        verify(studentRepository, never()).nextStudentNumberValue();
    }

    @Test
    void duplicateEmailIsCheckedWithoutCaseSensitivity() {
        StudentRegistrationRequest request = registrationRequest();
        when(studentRepository.existsByNic("123456789")).thenReturn(false);
        when(studentRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.registerStudent(request));
        verify(studentRepository, never()).save(any(Student.class));
        verify(studentRepository, never()).nextStudentNumberValue();
    }

    @Test
    void getStudentReturnsNotFoundForUnknownId() {
        when(studentRepository.findById(22L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudent(22L));
    }

    @Test
    void searchReturnsStudentSummariesAndPaginationInformation() {
        Student student = new Student();
        student.setId(10L);
        student.setFirstName("Jane");
        student.setLastName("Doe");
        student.setStudentNumber("AB2026-0001");
        student.setNic("123456789");
        student.setEmail("jane@example.com");

        PageRequest pageable = PageRequest.of(
                1,
                2,
                org.springframework.data.domain.Sort.by("lastName").ascending()
                        .and(org.springframework.data.domain.Sort.by("firstName").ascending())
        );
        when(studentRepository.searchStudents(eq("jane"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(student), pageable, 11));

        StudentSearchResponse response = studentService.searchStudents(" jane ", 1, 2);

        assertEquals(1, response.getItems().size());
        assertEquals("Jane", response.getItems().get(0).getFirstName());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(11, response.getTotalElements());
        assertEquals(6, response.getTotalPages());
        assertFalse(response.isLast());
        assertFalse(response.isFirst());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(studentRepository).searchStudents(eq("jane"), pageableCaptor.capture());
        assertEquals(1, pageableCaptor.getValue().getPageNumber());
        assertEquals(2, pageableCaptor.getValue().getPageSize());
    }

    private StudentRegistrationRequest registrationRequest() {
        StudentRegistrationRequest request = new StudentRegistrationRequest();
        request.setFirstName(" Jane ");
        request.setLastName(" Doe ");
        request.setNic(" 123456789 ");
        request.setEmail(" Jane@Example.com ");
        request.setPhone(" 0771234567 ");
        request.setAddress(" Main Street ");
        request.setDateOfBirth(LocalDate.of(2000, 1, 1));
        return request;
    }
}

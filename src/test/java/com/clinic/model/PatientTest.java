package com.clinic.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Patient}.
 */
class PatientTest {

    @Test
    @DisplayName("Patient creation with all fields")
    void testPatientCreation() {
        Patient patient = new Patient(1, "John Doe", LocalDate.of(1990, 5, 15),
                Gender.MALE, "555-0100", "john@example.com", "123 Main St");

        assertEquals(1, patient.getId());
        assertEquals("John Doe", patient.getFullName());
        assertEquals(LocalDate.of(1990, 5, 15), patient.getDateOfBirth());
        assertEquals(Gender.MALE, patient.getGender());
        assertEquals("555-0100", patient.getPhone());
        assertEquals("john@example.com", patient.getEmail());
        assertEquals("123 Main St", patient.getAddress());
    }

    @Test
    @DisplayName("Patient default constructor creates empty patient")
    void testDefaultConstructor() {
        Patient patient = new Patient();
        assertEquals(0, patient.getId());
        assertNull(patient.getFullName());
        assertNull(patient.getDateOfBirth());
    }

    @Test
    @DisplayName("Patient rejects null full name")
    void testNullNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Patient(1, null, LocalDate.now(), Gender.MALE, null, null, null));
    }

    @Test
    @DisplayName("Patient rejects blank full name")
    void testBlankNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Patient(1, "   ", LocalDate.now(), Gender.MALE, null, null, null));
    }

    @Test
    @DisplayName("Patients with same id are equal")
    void testEquality() {
        Patient p1 = new Patient(1, "John Doe", LocalDate.of(1990, 1, 1),
                Gender.MALE, null, null, null);
        Patient p2 = new Patient(1, "Jane Doe", LocalDate.of(1995, 6, 1),
                Gender.FEMALE, null, null, null);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    @DisplayName("Patients with different ids are not equal")
    void testInequality() {
        Patient p1 = new Patient(1, "John Doe", LocalDate.now(), Gender.MALE, null, null, null);
        Patient p2 = new Patient(2, "John Doe", LocalDate.now(), Gender.MALE, null, null, null);
        assertNotEquals(p1, p2);
    }

    @Test
    @DisplayName("toString contains patient name")
    void testToString() {
        Patient patient = new Patient(1, "John Doe", LocalDate.now(),
                Gender.MALE, null, null, null);
        String result = patient.toString();
        assertTrue(result.contains("John Doe"));
        assertTrue(result.contains("Patient"));
    }

    @Test
    @DisplayName("Setters update patient fields")
    void testSetters() {
        Patient patient = new Patient();
        patient.setId(5);
        patient.setFullName("Updated Name");
        patient.setGender(Gender.OTHER);
        patient.setPhone("555-9999");
        patient.setEmail("updated@example.com");
        patient.setAddress("456 Oak Ave");
        patient.setDateOfBirth(LocalDate.of(2000, 1, 1));

        assertEquals(5, patient.getId());
        assertEquals("Updated Name", patient.getFullName());
        assertEquals(Gender.OTHER, patient.getGender());
        assertEquals("555-9999", patient.getPhone());
        assertEquals("updated@example.com", patient.getEmail());
        assertEquals("456 Oak Ave", patient.getAddress());
        assertEquals(LocalDate.of(2000, 1, 1), patient.getDateOfBirth());
    }
}

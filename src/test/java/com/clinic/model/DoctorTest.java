package com.clinic.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Doctor}.
 */
class DoctorTest {

    @Test
    @DisplayName("Doctor creation with all fields")
    void testDoctorCreation() {
        Doctor doctor = new Doctor(1, "Dr. Smith", "Cardiology", "555-0200", "smith@clinic.com");

        assertEquals(1, doctor.getId());
        assertEquals("Dr. Smith", doctor.getFullName());
        assertEquals("Cardiology", doctor.getSpecialization());
        assertEquals("555-0200", doctor.getPhone());
        assertEquals("smith@clinic.com", doctor.getEmail());
    }

    @Test
    @DisplayName("Doctor default constructor creates empty doctor")
    void testDefaultConstructor() {
        Doctor doctor = new Doctor();
        assertEquals(0, doctor.getId());
        assertNull(doctor.getFullName());
    }

    @Test
    @DisplayName("Doctor rejects null full name")
    void testNullNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Doctor(1, null, "Cardiology", null, null));
    }

    @Test
    @DisplayName("Doctor rejects blank full name")
    void testBlankNameThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Doctor(1, "", "Cardiology", null, null));
    }

    @Test
    @DisplayName("Doctors with same id are equal")
    void testEquality() {
        Doctor d1 = new Doctor(1, "Dr. Smith", "Cardiology", null, null);
        Doctor d2 = new Doctor(1, "Dr. Jones", "Neurology", null, null);
        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    @DisplayName("Doctors with different ids are not equal")
    void testInequality() {
        Doctor d1 = new Doctor(1, "Dr. Smith", "Cardiology", null, null);
        Doctor d2 = new Doctor(2, "Dr. Smith", "Cardiology", null, null);
        assertNotEquals(d1, d2);
    }

    @Test
    @DisplayName("toString contains doctor name and specialization")
    void testToString() {
        Doctor doctor = new Doctor(1, "Dr. Smith", "Cardiology", null, null);
        String result = doctor.toString();
        assertTrue(result.contains("Dr. Smith"));
        assertTrue(result.contains("Cardiology"));
    }
}

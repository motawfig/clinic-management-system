package com.clinic.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Appointment}.
 * <p>
 * تركز اختبارات الموعد على سلامة العلاقات الأساسية والمدة لأنها تدخل في حساب التعارض.
 */
class AppointmentTest {

    private Patient createTestPatient() {
        return new Patient(1, "John Doe", LocalDate.of(1990, 1, 1),
                Gender.MALE, "555-0100", "john@example.com", "123 Main St");
    }

    private Doctor createTestDoctor() {
        return new Doctor(1, "Dr. Smith", "Cardiology", "555-0200", "smith@clinic.com");
    }

    @Test
    @DisplayName("Appointment creation with all fields")
    void testAppointmentCreation() {
        Patient patient = createTestPatient();
        Doctor doctor = createTestDoctor();
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 15, 10, 30);

        Appointment appt = new Appointment(1, patient, doctor, dateTime,
                AppointmentStatus.SCHEDULED, "Initial consultation");

        assertEquals(1, appt.getId());
        assertEquals(patient, appt.getPatient());
        assertEquals(doctor, appt.getDoctor());
        assertEquals(dateTime, appt.getAppointmentDateTime());
        assertEquals(30, appt.getDurationMinutes());
        assertEquals(AppointmentStatus.SCHEDULED, appt.getStatus());
        assertEquals("Initial consultation", appt.getNotes());
    }

    @Test
    @DisplayName("Appointment accepts positive duration")
    void testPositiveDuration() {
        Patient patient = createTestPatient();
        Doctor doctor = createTestDoctor();
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 15, 10, 30);

        Appointment appt = new Appointment(1, patient, doctor, dateTime, 45,
                AppointmentStatus.SCHEDULED, "Initial consultation");

        assertEquals(45, appt.getDurationMinutes());

        appt.setDurationMinutes(60);
        assertEquals(60, appt.getDurationMinutes());
    }

    @Test
    @DisplayName("Appointment rejects zero duration")
    void testZeroDurationThrowsException() {
        Patient patient = createTestPatient();
        Doctor doctor = createTestDoctor();
        assertThrows(IllegalArgumentException.class, () ->
                new Appointment(1, patient, doctor, LocalDateTime.now(), 0,
                        AppointmentStatus.SCHEDULED, null));
    }

    @Test
    @DisplayName("Appointment rejects negative duration")
    void testNegativeDurationThrowsException() {
        Patient patient = createTestPatient();
        Doctor doctor = createTestDoctor();

        assertThrows(IllegalArgumentException.class, () ->
                new Appointment(1, patient, doctor, LocalDateTime.now(), -15,
                        AppointmentStatus.SCHEDULED, null));
    }

    @Test
    @DisplayName("Appointment rejects null patient")
    void testNullPatientThrowsException() {
        Doctor doctor = createTestDoctor();
        assertThrows(IllegalArgumentException.class, () ->
                new Appointment(1, null, doctor, LocalDateTime.now(),
                        AppointmentStatus.SCHEDULED, null));
    }

    @Test
    @DisplayName("Appointment rejects null doctor")
    void testNullDoctorThrowsException() {
        Patient patient = createTestPatient();
        assertThrows(IllegalArgumentException.class, () ->
                new Appointment(1, patient, null, LocalDateTime.now(),
                        AppointmentStatus.SCHEDULED, null));
    }

    @Test
    @DisplayName("Appointments with same id are equal")
    void testEquality() {
        Patient patient = createTestPatient();
        Doctor doctor = createTestDoctor();
        Appointment a1 = new Appointment(1, patient, doctor, LocalDateTime.now(),
                AppointmentStatus.SCHEDULED, null);
        Appointment a2 = new Appointment(1, patient, doctor, LocalDateTime.now().plusHours(1),
                AppointmentStatus.CONFIRMED, "different");
        assertEquals(a1, a2);
    }

    @Test
    @DisplayName("toString contains patient and doctor names")
    void testToString() {
        Patient patient = createTestPatient();
        Doctor doctor = createTestDoctor();
        Appointment appt = new Appointment(1, patient, doctor, LocalDateTime.now(),
                AppointmentStatus.SCHEDULED, null);
        String result = appt.toString();
        assertTrue(result.contains("John Doe"));
        assertTrue(result.contains("Dr. Smith"));
        assertTrue(result.contains("durationMinutes=30"));
    }

    @Test
    @DisplayName("Appointment status can be updated")
    void testStatusUpdate() {
        Patient patient = createTestPatient();
        Doctor doctor = createTestDoctor();
        Appointment appt = new Appointment(1, patient, doctor, LocalDateTime.now(),
                AppointmentStatus.SCHEDULED, null);

        appt.setStatus(AppointmentStatus.COMPLETED);
        assertEquals(AppointmentStatus.COMPLETED, appt.getStatus());
    }
}

package com.clinic.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MedicalRecord}.
 */
class MedicalRecordTest {

    private Patient createTestPatient() {
        return new Patient(1, "John Doe", LocalDate.of(1990, 1, 1),
                Gender.MALE, "555-0100", "john@example.com", "123 Main St");
    }

    @Test
    @DisplayName("MedicalRecord creation with all fields")
    void testRecordCreation() {
        Patient patient = createTestPatient();
        LocalDateTime now = LocalDateTime.now();

        MedicalRecord record = new MedicalRecord(1, patient, "Hypertension",
                "Medication prescribed", "Follow up in 3 months", now);

        assertEquals(1, record.getId());
        assertEquals(patient, record.getPatient());
        assertEquals("Hypertension", record.getDiagnosis());
        assertEquals("Medication prescribed", record.getTreatment());
        assertEquals("Follow up in 3 months", record.getNotes());
        assertEquals(now, record.getCreatedAt());
    }

    @Test
    @DisplayName("MedicalRecord rejects null patient")
    void testNullPatientThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MedicalRecord(1, null, "Diagnosis", "Treatment", null, LocalDateTime.now()));
    }

    @Test
    @DisplayName("MedicalRecords with same id are equal")
    void testEquality() {
        Patient patient = createTestPatient();
        MedicalRecord r1 = new MedicalRecord(1, patient, "Diagnosis A", "Treatment A",
                null, LocalDateTime.now());
        MedicalRecord r2 = new MedicalRecord(1, patient, "Diagnosis B", "Treatment B",
                null, LocalDateTime.now());
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    @DisplayName("toString contains diagnosis")
    void testToString() {
        Patient patient = createTestPatient();
        MedicalRecord record = new MedicalRecord(1, patient, "Hypertension",
                "Medication", null, LocalDateTime.now());
        String result = record.toString();
        assertTrue(result.contains("Hypertension"));
        assertTrue(result.contains("MedicalRecord"));
    }

    @Test
    @DisplayName("Default constructor creates empty record")
    void testDefaultConstructor() {
        MedicalRecord record = new MedicalRecord();
        assertEquals(0, record.getId());
        assertNull(record.getPatient());
        assertNull(record.getDiagnosis());
    }

    @Test
    @DisplayName("Setters update record fields")
    void testSetters() {
        Patient patient = createTestPatient();
        MedicalRecord record = new MedicalRecord();
        record.setId(10);
        record.setPatient(patient);
        record.setDiagnosis("Flu");
        record.setTreatment("Rest and fluids");
        record.setNotes("Mild case");
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);

        assertEquals(10, record.getId());
        assertEquals(patient, record.getPatient());
        assertEquals("Flu", record.getDiagnosis());
        assertEquals("Rest and fluids", record.getTreatment());
        assertEquals("Mild case", record.getNotes());
        assertEquals(now, record.getCreatedAt());
    }
}

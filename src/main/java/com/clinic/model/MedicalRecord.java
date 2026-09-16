package com.clinic.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a medical record associated with a patient.
 */
public class MedicalRecord {

    private int id;
    private Patient patient;
    private String diagnosis;
    private String treatment;
    private String notes;
    private LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    public MedicalRecord() {
    }

    /**
     * Creates a new MedicalRecord.
     *
     * @param id        unique identifier
     * @param patient   the patient this record belongs to (must not be null)
     * @param diagnosis the diagnosis
     * @param treatment the treatment prescribed
     * @param notes     additional notes
     * @param createdAt timestamp when the record was created
     * @throws IllegalArgumentException if patient is null
     */
    public MedicalRecord(int id, Patient patient, String diagnosis, String treatment,
                         String notes, LocalDateTime createdAt) {
        setPatient(patient);
        this.id = id;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    /**
     * Sets the patient for this medical record.
     *
     * @param patient must not be null
     * @throws IllegalArgumentException if patient is null
     */
    public void setPatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient must not be null");
        }
        this.patient = patient;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecord that = (MedicalRecord) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getFullName() : "null") +
                ", diagnosis='" + diagnosis + '\'' +
                ", treatment='" + treatment + '\'' +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

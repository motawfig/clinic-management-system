package com.clinic.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a scheduled appointment between a patient and a doctor.
 */
public class Appointment {

    private int id;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime appointmentDateTime;
    private AppointmentStatus status;
    private String notes;

    /**
     * Default constructor.
     */
    public Appointment() {
    }

    /**
     * Creates a new Appointment.
     *
     * @param id                  unique identifier
     * @param patient             the patient (must not be null)
     * @param doctor              the doctor (must not be null)
     * @param appointmentDateTime date and time of the appointment
     * @param status              current status
     * @param notes               additional notes
     * @throws IllegalArgumentException if patient or doctor is null
     */
    public Appointment(int id, Patient patient, Doctor doctor,
                       LocalDateTime appointmentDateTime, AppointmentStatus status,
                       String notes) {
        setPatient(patient);
        setDoctor(doctor);
        this.id = id;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.notes = notes;
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
     * Sets the patient for this appointment.
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

    public Doctor getDoctor() {
        return doctor;
    }

    /**
     * Sets the doctor for this appointment.
     *
     * @param doctor must not be null
     * @throws IllegalArgumentException if doctor is null
     */
    public void setDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor must not be null");
        }
        this.doctor = doctor;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getFullName() : "null") +
                ", doctor=" + (doctor != null ? doctor.getFullName() : "null") +
                ", dateTime=" + appointmentDateTime +
                ", status=" + status +
                ", notes='" + notes + '\'' +
                '}';
    }
}
